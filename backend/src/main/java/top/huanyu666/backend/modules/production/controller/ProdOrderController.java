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
import top.huanyu666.backend.modules.base.entity.Material;
import top.huanyu666.backend.modules.base.mapper.BomDetailMapper;
import top.huanyu666.backend.modules.base.mapper.BomHeaderMapper;
import top.huanyu666.backend.modules.base.mapper.MaterialMapper;
import top.huanyu666.backend.modules.production.entity.*;
import top.huanyu666.backend.modules.production.mapper.*;
import top.huanyu666.backend.modules.production.service.ProdOrderService;

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
    private final ProdOrderService orderService;
    private final BomHeaderMapper bomHeaderMapper;
    private final BomDetailMapper bomDetailMapper;
    private final MaterialMapper materialMapper;

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
        ProdOrder order = orderService.getById(id);

        List<ProdPicking> pickings = orderService.getPickingsByOrderId(id);
        List<ProdFinish> finishings = orderService.getFinishingsByOrderId(id);

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("pickings", pickings);
        result.put("finishings", finishings);
        return ApiResponse.ok(result);
    }

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

    @SaCheckPermission("production:order:release")
    @PostMapping("/release/{id}")
    @Transactional
    public ApiResponse<String> release(@PathVariable Long id) {
        orderService.release(id);
        return ApiResponse.ok("下达成功");
    }

    @SaCheckPermission("production:order:view")
    @GetMapping("/material-requirements/{id}")
    public ApiResponse<List<ProdOrderBom>> materialRequirements(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getMaterialRequirements(id));
    }

    @SaCheckPermission("production:order:create")
    @PostMapping("/create-picking/{id}")
    @Transactional
    public ApiResponse<ProdPicking> createPicking(@PathVariable Long id,
                                                   @RequestBody ProdPicking picking) {
        return ApiResponse.ok(orderService.createPicking(id, picking));
    }

    @SaCheckPermission("production:order:create")
    @PostMapping("/create-finish/{id}")
    @Transactional
    public ApiResponse<ProdFinish> createFinish(@PathVariable Long id,
                                                 @RequestBody ProdFinish finish) {
        return ApiResponse.ok(orderService.createFinish(id, finish));
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

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/finish")
    @Transactional
    public ApiResponse<Void> finish(@PathVariable Long id) {
        orderService.finish(id);
        return ApiResponse.ok();
    }

    // ==================== 辅助查询 ====================

    /** 查询所有已配置 BOM 的成品物料，供工单创建时选择 */
    @SaCheckPermission("production:order:view")
    @GetMapping("/bom-products")
    public ApiResponse<List<Map<String, Object>>> bomProducts() {
        List<BomHeader> headers = bomHeaderMapper.selectList(
                new LambdaQueryWrapper<BomHeader>().eq(BomHeader::getStatus, 1));
        List<Long> materialIds = headers.stream().map(BomHeader::getProductMaterialId).distinct().toList();
        if (materialIds.isEmpty()) return ApiResponse.ok(List.of());
        List<Material> materials = materialMapper.selectBatchIds(materialIds);
        return ApiResponse.ok(materials.stream()
                .filter(m -> m.getStatus() == 1)
                .map(m -> Map.<String, Object>of("id", m.getId(), "name", m.getName(), "code", m.getCode()))
                .toList());
    }

    /** 查询某物料的 BOM 明细，供创建时预览 */
    @SaCheckPermission("production:order:view")
    @GetMapping("/bom-products/{materialId}/bom")
    public ApiResponse<List<Map<String, Object>>> bomDetail(@PathVariable Long materialId) {
        BomHeader header = bomHeaderMapper.selectOne(
                new LambdaQueryWrapper<BomHeader>()
                        .eq(BomHeader::getProductMaterialId, materialId)
                        .eq(BomHeader::getStatus, 1)
                        .orderByDesc(BomHeader::getCreateTime)
                        .last("LIMIT 1"));
        if (header == null) return ApiResponse.ok(List.of());
        return ApiResponse.ok(bomDetailMapper.selectList(
                new LambdaQueryWrapper<BomDetail>()
                        .eq(BomDetail::getBomId, header.getId()))
                .stream().map(d -> {
                    Material m = materialMapper.selectById(d.getMaterialId());
                    return Map.<String, Object>of(
                            "materialId", d.getMaterialId(),
                            "materialName", m != null ? m.getName() : "未知",
                            "unitUsage", d.getQuantity(),
                            "unit", d.getUnit() != null ? d.getUnit() : "");
                }).toList());
    }
}
