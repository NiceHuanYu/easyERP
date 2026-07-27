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
import top.huanyu666.backend.modules.base.entity.Bom;
import top.huanyu666.backend.modules.base.mapper.BomMapper;
import top.huanyu666.backend.modules.production.entity.*;
import top.huanyu666.backend.modules.production.mapper.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 生产工单管理
 */
@RestController
@RequestMapping("/api/v1/production/orders")
@RequiredArgsConstructor
@Slf4j
public class ProdOrderController {

    private final ProdOrderMapper orderMapper;
    private final ProdOrderBomMapper orderBomMapper;
    private final ProdPickingMapper pickingMapper;
    private final ProdPickingItemMapper pickingItemMapper;
    private final ProdFinishMapper finishMapper;
    private final ProdFinishItemMapper finishItemMapper;
    private final BomMapper bomMapper;

    // ==================== 基础 CRUD ====================

    @SaCheckPermission("production:order:list")
    @GetMapping
    public ApiResponse<PageResult<ProdOrder>> list(PageParam param,
                                                   @RequestParam(required = false) Long materialId,
                                                   @RequestParam(required = false) String status) {
        LambdaQueryWrapper<ProdOrder> qw = new LambdaQueryWrapper<>();
        if (materialId != null) {
            qw.eq(ProdOrder::getMaterialId, materialId);
        }
        if (status != null && !status.isBlank()) {
            qw.eq(ProdOrder::getStatus, status);
        }
        qw.orderByDesc(ProdOrder::getCreateTime);
        Page<ProdOrder> page = orderMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("production:order:list")
    @GetMapping("/{id}")
    public ApiResponse<ProdOrder> detail(@PathVariable Long id) {
        ProdOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        return ApiResponse.ok(order);
    }

    @SaCheckPermission("production:order:create")
    @PostMapping
    public ApiResponse<ProdOrder> create(@RequestBody ProdOrder order) {
        order.setStatus("DRAFT");
        orderMapper.insert(order);
        return ApiResponse.ok(order);
    }

    // ==================== 业务操作 ====================

    /**
     * 下达工单：DRAFT → RELEASED，从 t_base_bom 生成工单物料需求
     */
    @SaCheckPermission("production:order:release")
    @PostMapping("/release/{id}")
    @Transactional
    public ApiResponse<String> release(@PathVariable Long id) {
        ProdOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (!"DRAFT".equals(order.getStatus())) {
            throw new BusinessException("仅草稿状态的工单可下达");
        }

        // 从 t_base_bom 查 BOM
        LambdaQueryWrapper<Bom> bomQw = new LambdaQueryWrapper<>();
        bomQw.eq(Bom::getParentMaterialId, order.getMaterialId());
        List<Bom> bomList = bomMapper.selectList(bomQw);

        if (bomList.isEmpty()) {
            throw new BusinessException("该物料未配置 BOM，无法下达");
        }

        // 生成工单物料需求
        for (Bom bom : bomList) {
            ProdOrderBom orderBom = new ProdOrderBom();
            orderBom.setOrderId(id);
            orderBom.setMaterialId(bom.getChildMaterialId());
            // requiredQty = planQuantity * bom.quantity * (1 + lossRate)
            BigDecimal lossFactor = BigDecimal.ONE.add(
                    bom.getLossRate() != null ? bom.getLossRate() : BigDecimal.ZERO);
            BigDecimal requiredQty = order.getPlanQuantity()
                    .multiply(bom.getQuantity())
                    .multiply(lossFactor)
                    .setScale(4, RoundingMode.HALF_UP);
            orderBom.setRequiredQty(requiredQty);
            orderBom.setPickedQty(BigDecimal.ZERO);
            orderBom.setCreateTime(LocalDateTime.now());
            orderBom.setUpdateTime(LocalDateTime.now());
            orderBomMapper.insert(orderBom);
        }

        order.setStatus("RELEASED");
        orderMapper.updateById(order);

        log.info("工单 {} 已下达，生成 {} 条物料需求", order.getOrderNo(), bomList.size());
        return ApiResponse.ok("下达成功");
    }

    /**
     * 查询工单物料需求
     */
    @SaCheckPermission("production:order:list")
    @GetMapping("/material-requirements/{id}")
    public ApiResponse<List<ProdOrderBom>> materialRequirements(@PathVariable Long id) {
        ProdOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        LambdaQueryWrapper<ProdOrderBom> qw = new LambdaQueryWrapper<>();
        qw.eq(ProdOrderBom::getOrderId, id);
        return ApiResponse.ok(orderBomMapper.selectList(qw));
    }

    /**
     * 创建领料单：查工单需求（未领数量 > 0）
     */
    @SaCheckPermission("production:order:create")
    @PostMapping("/create-picking/{id}")
    @Transactional
    public ApiResponse<ProdPicking> createPicking(@PathVariable Long id,
                                                   @RequestBody ProdPicking picking) {
        ProdOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }

        // 查未领完的物料需求
        LambdaQueryWrapper<ProdOrderBom> bomQw = new LambdaQueryWrapper<>();
        bomQw.eq(ProdOrderBom::getOrderId, id);
        bomQw.apply("required_qty - picked_qty > 0");
        List<ProdOrderBom> bomList = orderBomMapper.selectList(bomQw);

        if (bomList.isEmpty()) {
            throw new BusinessException("该工单没有待领物料");
        }

        // 创建领料单主表
        picking.setOrderId(id);
        picking.setStatus("DRAFT");
        picking.setPickingDate(LocalDate.now());
        pickingMapper.insert(picking);

        // 创建领料单明细
        for (ProdOrderBom bom : bomList) {
            BigDecimal remaining = bom.getRequiredQty().subtract(bom.getPickedQty());
            ProdPickingItem item = new ProdPickingItem();
            item.setPickingId(picking.getId());
            item.setMaterialId(bom.getMaterialId());
            item.setRequestQty(remaining);
            item.setActualQty(BigDecimal.ZERO);
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            pickingItemMapper.insert(item);
        }

        log.info("工单 {} 创建领料单 {}，{} 条明细", order.getOrderNo(), picking.getPickingNo(), bomList.size());
        return ApiResponse.ok(picking);
    }

    /**
     * 创建完工入库单
     */
    @SaCheckPermission("production:order:create")
    @PostMapping("/create-finish/{id}")
    @Transactional
    public ApiResponse<ProdFinish> createFinish(@PathVariable Long id,
                                                 @RequestBody ProdFinish finish) {
        ProdOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }

        finish.setOrderId(id);
        finish.setStatus("DRAFT");
        finish.setFinishDate(LocalDate.now());
        finishMapper.insert(finish);

        // 创建完工入库明细（以工单产品物料）
        ProdFinishItem item = new ProdFinishItem();
        item.setFinishId(finish.getId());
        item.setMaterialId(order.getMaterialId());
        item.setQuantity(order.getPlanQuantity());
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        finishItemMapper.insert(item);

        log.info("工单 {} 创建完工入库单 {}", order.getOrderNo(), finish.getFinishNo());
        return ApiResponse.ok(finish);
    }

    /**
     * 修改（仅草稿）
     */
    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody ProdOrder order) {
        ProdOrder exist = orderMapper.selectById(id);
        if (exist == null) throw new BusinessException("工单不存在");
        if (!"DRAFT".equals(exist.getStatus())) throw new BusinessException("只有草稿状态可编辑");
        order.setId(id);
        orderMapper.updateById(order);
        return ApiResponse.ok();
    }

    /**
     * 删除（仅草稿）
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        ProdOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("工单不存在");
        if (!"DRAFT".equals(order.getStatus())) throw new BusinessException("只有草稿状态可删除");
        orderBomMapper.delete(new LambdaQueryWrapper<ProdOrderBom>().eq(ProdOrderBom::getOrderId, id));
        orderMapper.deleteById(id);
        return ApiResponse.ok();
    }

    /**
     * 完工
     */
    @PostMapping("/{id}/finish")
    @Transactional
    public ApiResponse<Void> finish(@PathVariable Long id) {
        ProdOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("工单不存在");
        order.setStatus("COMPLETED");
        orderMapper.updateById(order);
        return ApiResponse.ok();
    }
}
