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
import top.huanyu666.backend.modules.base.entity.Material;
import top.huanyu666.backend.modules.base.entity.Warehouse;
import top.huanyu666.backend.modules.base.mapper.MaterialMapper;
import top.huanyu666.backend.modules.base.mapper.WarehouseMapper;
import top.huanyu666.backend.modules.inventory.service.InvStockService;
import top.huanyu666.backend.modules.production.entity.*;
import top.huanyu666.backend.modules.production.mapper.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final ProdOrderMapper orderMapper;
    private final MaterialMapper materialMapper;
    private final WarehouseMapper warehouseMapper;
    private final InvStockService stockService;

    // ==================== 基础 CRUD ====================

    @SaCheckPermission("production:order:view")
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
        // 填充工单号和物料汇总
        for (ProdPicking p : page.getRecords()) {
            // 工单号
            if (p.getOrderId() != null) {
                ProdOrder order = orderMapper.selectById(p.getOrderId());
                p.setOrderNo(order != null ? order.getOrderNo() : "");
            }
            // 仓库名
            if (p.getWarehouseId() != null) {
                Warehouse wh = warehouseMapper.selectById(p.getWarehouseId());
                p.setWarehouseName(wh != null ? wh.getName() : "");
            }
            // 物料汇总：查明细 → 批量查物料名 → 拼接
            List<ProdPickingItem> items = pickingItemMapper.selectList(
                    new LambdaQueryWrapper<ProdPickingItem>().eq(ProdPickingItem::getPickingId, p.getId()));
            if (!items.isEmpty()) {
                List<Long> mIds = items.stream().map(ProdPickingItem::getMaterialId).distinct().toList();
                java.util.Map<Long, String> nameMap = materialMapper.selectBatchIds(mIds).stream()
                        .collect(java.util.stream.Collectors.toMap(Material::getId, Material::getName, (a, b) -> a));
                String summary = items.stream()
                        .map(i -> nameMap.getOrDefault(i.getMaterialId(), "") + "×" + i.getActualQty())
                        .reduce((a, b) -> a + "、 " + b)
                        .orElse("");
                p.setMaterialSummary(summary);
            }
        }
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    /** 详情（含明细），字段对齐前端表单 */
    @SaCheckPermission("production:order:view")
    @GetMapping("/{id}")
    public ApiResponse<java.util.Map<String, Object>> detail(@PathVariable Long id) {
        ProdPicking picking = pickingMapper.selectById(id);
        if (picking == null) throw new BusinessException("领料单不存在");
        List<ProdPickingItem> items = pickingItemMapper.selectList(
                new LambdaQueryWrapper<ProdPickingItem>().eq(ProdPickingItem::getPickingId, id));

        // 批量查物料名
        final java.util.Map<Long, Material> matMap;
        if (!items.isEmpty()) {
            List<Long> mIds = items.stream().map(ProdPickingItem::getMaterialId).distinct().toList();
            matMap = materialMapper.selectBatchIds(mIds).stream()
                    .collect(java.util.stream.Collectors.toMap(Material::getId, m -> m, (a, b) -> a));
        } else {
            matMap = java.util.Collections.emptyMap();
        }

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", picking.getId());
        result.put("pickingNo", picking.getPickingNo());
        result.put("orderId", picking.getOrderId());
        // 工单号
        if (picking.getOrderId() != null) {
            ProdOrder order = orderMapper.selectById(picking.getOrderId());
            result.put("orderNo", order != null ? order.getOrderNo() : "");
        }
        result.put("warehouseId", picking.getWarehouseId());
        result.put("pickingDate", picking.getPickingDate() != null ? picking.getPickingDate().toString() : "");
        result.put("status", picking.getStatus());
        result.put("lines", items.stream().map(i -> {
            Material m = matMap.get(i.getMaterialId());
            java.util.Map<String, Object> line = new java.util.HashMap<>();
            line.put("id", i.getId());
            line.put("materialId", i.getMaterialId());
            line.put("materialName", m != null ? m.getName() : "");
            line.put("materialCode", m != null ? m.getCode() : "");
            line.put("unit", m != null ? m.getUnit() : "");
            line.put("requestQty", i.getRequestQty());
            line.put("actualQty", i.getActualQty());
            line.put("pickingQuantity", i.getActualQty());
            // 前端期望的字段名
            line.put("requiredQuantity", i.getRequestQty());
            line.put("pickedQuantity", i.getActualQty());
            return line;
        }).toList());
        return ApiResponse.ok(result);
    }

    @SaCheckPermission("production:order:create")
    @PostMapping
    public ApiResponse<ProdPicking> create(@RequestBody java.util.Map<String, Object> body) {
        ProdPicking picking = mapToPicking(body);
        picking.setStatus("DRAFT");
        if (picking.getPickingNo() == null || picking.getPickingNo().isBlank()) {
            picking.setPickingNo(CodeGenerator.generate("PK", () -> {
                ProdPicking last = pickingMapper.selectOne(
                        new LambdaQueryWrapper<ProdPicking>()
                                .select(ProdPicking::getPickingNo)
                                .orderByDesc(ProdPicking::getPickingNo)
                                .last("LIMIT 1"));
                return last != null ? last.getPickingNo() : null;
            }));
        }
        pickingMapper.insert(picking);
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> lines = (java.util.List<java.util.Map<String, Object>>) body.get("lines");
        if (lines != null) savePickingItems(picking.getId(), lines);
        return ApiResponse.ok(picking);
    }

    @SaCheckPermission("production:order:edit")
    @PutMapping("/{id}")
    public ApiResponse<ProdPicking> update(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        ProdPicking exist = pickingMapper.selectById(id);
        if (exist == null) throw new BusinessException("领料单不存在");
        if (!"DRAFT".equals(exist.getStatus())) throw new BusinessException("仅草稿状态可修改");
        ProdPicking picking = mapToPicking(body);
        picking.setId(id);
        pickingMapper.updateById(picking);
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> lines = (java.util.List<java.util.Map<String, Object>>) body.get("lines");
        if (lines != null) {
            pickingItemMapper.delete(new LambdaQueryWrapper<ProdPickingItem>().eq(ProdPickingItem::getPickingId, id));
            savePickingItems(id, lines);
        }
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

            // 扣减库存（统一由 InvStockService 处理）
            stockService.deduct(item.getMaterialId(), picking.getWarehouseId(),
                    item.getActualQty(), picking.getPickingNo(), "PICKING_OUT");

            // 更新工单BOM已领数量
            LambdaQueryWrapper<ProdOrderBom> bomQw = new LambdaQueryWrapper<>();
            bomQw.eq(ProdOrderBom::getOrderId, picking.getOrderId());
            bomQw.eq(ProdOrderBom::getMaterialId, item.getMaterialId());
            ProdOrderBom orderBom = orderBomMapper.selectOne(bomQw);
            if (orderBom != null) {
                BigDecimal currentPicked = orderBom.getPickedQty() != null
                        ? orderBom.getPickedQty() : BigDecimal.ZERO;
                orderBom.setPickedQty(currentPicked.add(item.getActualQty()));
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
    @SaCheckPermission("production:order:delete")
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

    private ProdPicking mapToPicking(java.util.Map<String, Object> body) {
        ProdPicking p = new ProdPicking();
        if (body.containsKey("id")) p.setId(Long.valueOf(body.get("id").toString()));
        if (body.containsKey("orderId")) p.setOrderId(Long.valueOf(body.get("orderId").toString()));
        if (body.containsKey("warehouseId")) p.setWarehouseId(Long.valueOf(body.get("warehouseId").toString()));
        if (body.containsKey("pickingDate") && body.get("pickingDate") != null && !body.get("pickingDate").toString().isBlank()) p.setPickingDate(LocalDate.parse(body.get("pickingDate").toString()));
        if (body.containsKey("status")) p.setStatus((String) body.get("status"));
        return p;
    }

    private void savePickingItems(Long pickingId, java.util.List<java.util.Map<String, Object>> lines) {
        for (java.util.Map<String, Object> line : lines) {
            ProdPickingItem item = new ProdPickingItem();
            item.setPickingId(pickingId);
            item.setMaterialId(Long.valueOf(line.get("materialId").toString()));
            // 兼容 quantity 和 pickingQuantity 两种字段名
            BigDecimal qty = new BigDecimal(
                    line.get("quantity") != null
                            ? line.get("quantity").toString()
                            : (line.get("pickingQuantity") != null
                                    ? line.get("pickingQuantity").toString() : "0"));
            item.setRequestQty(qty);
            item.setActualQty(qty);
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            pickingItemMapper.insert(item);
        }
    }
}
