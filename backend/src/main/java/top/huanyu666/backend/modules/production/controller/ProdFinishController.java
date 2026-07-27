package top.huanyu666.backend.modules.production.controller;

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
 * 完工入库单管理
 */
@RestController
@RequestMapping("/api/v1/production/finishings")
@RequiredArgsConstructor
@Slf4j
public class ProdFinishController {

    private final ProdFinishMapper finishMapper;
    private final ProdFinishItemMapper finishItemMapper;
    private final ProdOrderMapper orderMapper;
    private final InvStockMapper stockMapper;
    private final InvTransactionMapper transactionMapper;

    // ==================== 基础 CRUD ====================

    @GetMapping
    public ApiResponse<PageResult<ProdFinish>> list(PageParam param,
                                                     @RequestParam(required = false) Long orderId,
                                                     @RequestParam(required = false) String status) {
        LambdaQueryWrapper<ProdFinish> qw = new LambdaQueryWrapper<>();
        if (orderId != null) {
            qw.eq(ProdFinish::getOrderId, orderId);
        }
        if (status != null && !status.isBlank()) {
            qw.eq(ProdFinish::getStatus, status);
        }
        qw.orderByDesc(ProdFinish::getCreateTime);
        Page<ProdFinish> page = finishMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @PostMapping
    public ApiResponse<ProdFinish> create(@RequestBody ProdFinish finish) {
        finish.setStatus("DRAFT");
        finishMapper.insert(finish);
        return ApiResponse.ok(finish);
    }

    @PutMapping("/{id}")
    public ApiResponse<ProdFinish> update(@PathVariable Long id, @RequestBody ProdFinish finish) {
        ProdFinish exist = finishMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("完工入库单不存在");
        }
        if (!"DRAFT".equals(exist.getStatus())) {
            throw new BusinessException("仅草稿状态可修改");
        }
        finish.setId(id);
        finishMapper.updateById(finish);
        return ApiResponse.ok(finishMapper.selectById(id));
    }

    // ==================== 业务操作 ====================

    /**
     * 确认完工入库：DRAFT → CONFIRMED，增加库存，记录流水
     */
    @PostMapping("/confirm/{id}")
    @Transactional
    public ApiResponse<String> confirm(@PathVariable Long id) {
        ProdFinish finish = finishMapper.selectById(id);
        if (finish == null) {
            throw new BusinessException("完工入库单不存在");
        }
        if (!"DRAFT".equals(finish.getStatus())) {
            throw new BusinessException("仅草稿状态可确认");
        }

        // 查完工入库明细
        LambdaQueryWrapper<ProdFinishItem> itemQw = new LambdaQueryWrapper<>();
        itemQw.eq(ProdFinishItem::getFinishId, id);
        List<ProdFinishItem> items = finishItemMapper.selectList(itemQw);

        if (items.isEmpty()) {
            throw new BusinessException("完工入库单无明细");
        }

        for (ProdFinishItem item : items) {
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("完工入库明细数量必须大于0");
            }

            // 增加库存（不存在则创建）
            LambdaQueryWrapper<InvStock> stockQw = new LambdaQueryWrapper<>();
            stockQw.eq(InvStock::getMaterialId, item.getMaterialId());
            stockQw.eq(InvStock::getWarehouseId, finish.getWarehouseId());
            InvStock stock = stockMapper.selectOne(stockQw);
            if (stock == null) {
                stock = new InvStock();
                stock.setMaterialId(item.getMaterialId());
                stock.setWarehouseId(finish.getWarehouseId());
                stock.setQuantity(BigDecimal.ZERO);
                stock.setAvailableQty(BigDecimal.ZERO);
                stock.setLockedQty(BigDecimal.ZERO);
                stock.setCreateTime(LocalDateTime.now());
                stock.setUpdateTime(LocalDateTime.now());
                stockMapper.insert(stock);
            }

            stock.setQuantity(stock.getQuantity().add(item.getQuantity()));
            stock.setAvailableQty(stock.getAvailableQty().add(item.getQuantity()));
            stock.setUpdateTime(LocalDateTime.now());
            stockMapper.updateById(stock);

            // 记录库存流水
            InvTransaction tx = new InvTransaction();
            tx.setMaterialId(item.getMaterialId());
            tx.setWarehouseId(finish.getWarehouseId());
            tx.setType("FINISH_IN");
            tx.setQuantity(item.getQuantity());
            tx.setCurrentStock(stock.getQuantity());
            tx.setSourceNo(finish.getFinishNo());
            tx.setSourceType("PROD_FINISH");
            tx.setCreateTime(LocalDateTime.now());
            transactionMapper.insert(tx);
        }

        // 更新工单完工数量
        ProdOrder order = orderMapper.selectById(finish.getOrderId());
        if (order != null) {
            BigDecimal totalFinishQty = items.stream()
                    .map(ProdFinishItem::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal currentFinish = order.getFinishQuantity() != null
                    ? order.getFinishQuantity() : BigDecimal.ZERO;
            order.setFinishQuantity(currentFinish.add(totalFinishQty));
            orderMapper.updateById(order);
        }

        finish.setStatus("CONFIRMED");
        finishMapper.updateById(finish);

        log.info("完工入库单 {} 已确认，{} 条明细", finish.getFinishNo(), items.size());
        return ApiResponse.ok("确认成功");
    }
}
