package top.huanyu666.backend.modules.production.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import top.huanyu666.backend.modules.inventory.entity.InvStock;
import top.huanyu666.backend.modules.inventory.entity.InvTransaction;
import top.huanyu666.backend.modules.inventory.mapper.InvStockMapper;
import top.huanyu666.backend.modules.inventory.mapper.InvTransactionMapper;
import top.huanyu666.backend.modules.production.entity.*;
import top.huanyu666.backend.modules.production.mapper.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 领料单管理
 */
@RestController
@RequestMapping("/api/v1/production/pickings")
@RequiredArgsConstructor
@Slf4j
public class ProdPickingController {

    private final ProdPickingMapper pickingMapper;
    private final ProdPickingItemMapper pickingItemMapper;
    private final ProdOrderBomMapper orderBomMapper;
    private final InvStockMapper stockMapper;
    private final InvTransactionMapper transactionMapper;

    // ==================== 基础 CRUD ====================

    @SaCheckPermission("production:picking:list")
    @GetMapping
    public ApiResponse<PageResult<ProdPicking>> list(PageParam param,
                                                      @RequestParam(required = false) Long orderId,
                                                      @RequestParam(required = false) String status) {
        LambdaQueryWrapper<ProdPicking> qw = new LambdaQueryWrapper<>();
        if (orderId != null) {
            qw.eq(ProdPicking::getOrderId, orderId);
        }
        if (status != null && !status.isBlank()) {
            qw.eq(ProdPicking::getStatus, status);
        }
        qw.orderByDesc(ProdPicking::getCreateTime);
        Page<ProdPicking> page = pickingMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("production:order:create")
    @PostMapping
    public ApiResponse<ProdPicking> create(@RequestBody ProdPicking picking) {
        picking.setStatus("DRAFT");
        pickingMapper.insert(picking);
        return ApiResponse.ok(picking);
    }

    @SaCheckPermission("production:order:create")
    @PutMapping("/{id}")
    public ApiResponse<ProdPicking> update(@PathVariable Long id, @RequestBody ProdPicking picking) {
        ProdPicking exist = pickingMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("领料单不存在");
        }
        if (!"DRAFT".equals(exist.getStatus())) {
            throw new BusinessException("仅草稿状态可修改");
        }
        picking.setId(id);
        pickingMapper.updateById(picking);
        return ApiResponse.ok(pickingMapper.selectById(id));
    }

    // ==================== 业务操作 ====================

    /**
     * 确认领料：DRAFT → CONFIRMED，扣减库存，记录流水
     */
    @SaCheckPermission("production:picking:confirm")
    @PostMapping("/confirm/{id}")
    @Transactional
    public ApiResponse<String> confirm(@PathVariable Long id) {
        ProdPicking picking = pickingMapper.selectById(id);
        if (picking == null) {
            throw new BusinessException("领料单不存在");
        }
        if (!"DRAFT".equals(picking.getStatus())) {
            throw new BusinessException("仅草稿状态可确认");
        }

        // 查领料明细
        LambdaQueryWrapper<ProdPickingItem> itemQw = new LambdaQueryWrapper<>();
        itemQw.eq(ProdPickingItem::getPickingId, id);
        List<ProdPickingItem> items = pickingItemMapper.selectList(itemQw);

        if (items.isEmpty()) {
            throw new BusinessException("领料单无明细");
        }

        for (ProdPickingItem item : items) {
            if (item.getActualQty() == null || item.getActualQty().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("领料明细实际数量必须大于0");
            }

            // 扣减库存
            LambdaQueryWrapper<InvStock> stockQw = new LambdaQueryWrapper<>();
            stockQw.eq(InvStock::getMaterialId, item.getMaterialId());
            stockQw.eq(InvStock::getWarehouseId, picking.getWarehouseId());
            InvStock stock = stockMapper.selectOne(stockQw);
            if (stock == null) {
                throw new BusinessException("物料库存不存在，materialId=" + item.getMaterialId());
            }
            if (stock.getAvailableQty().compareTo(item.getActualQty()) < 0) {
                throw new BusinessException("可用库存不足，materialId=" + item.getMaterialId());
            }

            stock.setQuantity(stock.getQuantity().subtract(item.getActualQty()));
            stock.setAvailableQty(stock.getAvailableQty().subtract(item.getActualQty()));
            stock.setUpdateTime(LocalDateTime.now());
            stockMapper.updateById(stock);

            // 记录库存流水
            InvTransaction tx = new InvTransaction();
            tx.setMaterialId(item.getMaterialId());
            tx.setWarehouseId(picking.getWarehouseId());
            tx.setType("PICKING_OUT");
            tx.setQuantity(item.getActualQty().negate());
            tx.setCurrentStock(stock.getQuantity());
            tx.setSourceNo(picking.getPickingNo());
            tx.setSourceType("PROD_PICKING");
            tx.setCreateTime(LocalDateTime.now());
            transactionMapper.insert(tx);

            // 更新工单BOM已领数量
            LambdaQueryWrapper<ProdOrderBom> bomQw = new LambdaQueryWrapper<>();
            bomQw.eq(ProdOrderBom::getOrderId, picking.getOrderId());
            bomQw.eq(ProdOrderBom::getMaterialId, item.getMaterialId());
            ProdOrderBom orderBom = orderBomMapper.selectOne(bomQw);
            if (orderBom != null) {
                orderBom.setPickedQty(orderBom.getPickedQty().add(item.getActualQty()));
                orderBom.setUpdateTime(LocalDateTime.now());
                orderBomMapper.updateById(orderBom);
            }
        }

        picking.setStatus("CONFIRMED");
        pickingMapper.updateById(picking);

        log.info("领料单 {} 已确认，{} 条明细", picking.getPickingNo(), items.size());
        return ApiResponse.ok("确认成功");
    }

    /**
     * 删除（仅草稿）
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        ProdPicking p = pickingMapper.selectById(id);
        if (p == null) throw new BusinessException("领料单不存在");
        if (!"DRAFT".equals(p.getStatus())) throw new BusinessException("只有草稿状态可删除");
        pickingItemMapper.delete(new LambdaQueryWrapper<ProdPickingItem>().eq(ProdPickingItem::getPickingId, id));
        pickingMapper.deleteById(id);
        return ApiResponse.ok();
    }
}
