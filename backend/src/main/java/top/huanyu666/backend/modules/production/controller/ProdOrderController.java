package top.huanyu666.backend.modules.production.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.utils.CodeGenerator;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import top.huanyu666.backend.modules.base.entity.BomDetail;
import top.huanyu666.backend.modules.base.entity.BomHeader;
import top.huanyu666.backend.modules.base.mapper.BomDetailMapper;
import top.huanyu666.backend.modules.base.mapper.BomHeaderMapper;
import top.huanyu666.backend.modules.production.entity.*;
import top.huanyu666.backend.modules.production.mapper.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final BomHeaderMapper bomHeaderMapper;
    private final BomDetailMapper bomDetailMapper;

    // ==================== 基础 CRUD ====================

    @SaCheckPermission("production:order:view")
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

    @SaCheckPermission("production:order:view")
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        ProdOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }

        List<ProdPicking> pickings = pickingMapper.selectList(
                new LambdaQueryWrapper<ProdPicking>().eq(ProdPicking::getOrderId, id));
        List<ProdFinish> finishings = finishMapper.selectList(
                new LambdaQueryWrapper<ProdFinish>().eq(ProdFinish::getOrderId, id));

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("pickings", pickings);
        result.put("finishings", finishings);
        return ApiResponse.ok(result);
    }

    /**
     * 工单物料需求（别名）
     */
    @SaCheckPermission("production:order:view")
    @GetMapping("/{id}/materials")
    public ApiResponse<List<ProdOrderBom>> materials(@PathVariable Long id) {
        return materialRequirements(id);
    }

    @SaCheckPermission("production:order:create")
    @PostMapping
    public ApiResponse<ProdOrder> create(@RequestBody ProdOrder order) {
        order.setStatus("DRAFT");
        if (order.getOrderNo() == null || order.getOrderNo().isBlank()) {
            order.setOrderNo(CodeGenerator.generate("MO", () -> {
                ProdOrder last = orderMapper.selectOne(
                        new LambdaQueryWrapper<ProdOrder>()
                                .select(ProdOrder::getOrderNo)
                                .orderByDesc(ProdOrder::getOrderNo)
                                .last("LIMIT 1"));
                return last != null ? last.getOrderNo() : null;
            }));
        }
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

        // 从新 BOM 头行结构查物料需求
        BomHeader bomHeader = bomHeaderMapper.selectOne(
                new LambdaQueryWrapper<BomHeader>()
                        .eq(BomHeader::getProductMaterialId, order.getMaterialId())
                        .eq(BomHeader::getStatus, 1)
                        .orderByDesc(BomHeader::getCreateTime)
                        .last("LIMIT 1"));

        if (bomHeader == null) {
            throw new BusinessException("该物料未配置 BOM，无法下达");
        }

        List<BomDetail> details = bomDetailMapper.selectList(
                new LambdaQueryWrapper<BomDetail>().eq(BomDetail::getBomId, bomHeader.getId()));

        if (details.isEmpty()) {
            throw new BusinessException("BOM 无明细，无法下达");
        }

        for (BomDetail detail : details) {
            ProdOrderBom orderBom = new ProdOrderBom();
            orderBom.setOrderId(id);
            orderBom.setMaterialId(detail.getMaterialId());
            BigDecimal requiredQty = order.getPlanQuantity()
                    .multiply(detail.getQuantity())
                    .setScale(4, RoundingMode.HALF_UP);
            orderBom.setRequiredQty(requiredQty);
            orderBom.setPickedQty(BigDecimal.ZERO);
            orderBom.setCreateTime(LocalDateTime.now());
            orderBom.setUpdateTime(LocalDateTime.now());
            orderBomMapper.insert(orderBom);
        }

        order.setStatus("RELEASED");
        orderMapper.updateById(order);

        log.info("工单 {} 已下达，生成 {} 条物料需求", order.getOrderNo(), details.size());
        return ApiResponse.ok("下达成功");
    }

    /**
     * 查询工单物料需求
     */
    @SaCheckPermission("production:order:view")
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
