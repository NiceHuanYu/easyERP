package top.huanyu666.backend.modules.production.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
    private final MaterialMapper materialMapper;
    private final WarehouseMapper warehouseMapper;
    private final InvStockService stockService;

    // ==================== 基础 CRUD ====================

    @SaCheckPermission("production:order:view")
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
        // 填充工单号 + 前端字段名映射 + 物料/数量
        for (ProdFinish f : page.getRecords()) {
            // 前端字段名映射（前端用 finishingNo/finishingDate，表字段是 finishNo/finishDate）
            f.setFinishingNo(f.getFinishNo());
            f.setFinishingDate(f.getFinishDate() != null ? f.getFinishDate().toString() : "");
            // 工单号
            if (f.getOrderId() != null) {
                ProdOrder order = orderMapper.selectById(f.getOrderId());
                f.setOrderNo(order != null ? order.getOrderNo() : "");
            }
            // 仓库名
            if (f.getWarehouseId() != null) {
                Warehouse wh = warehouseMapper.selectById(f.getWarehouseId());
                f.setWarehouseName(wh != null ? wh.getName() : "");
            }
            // 物料名称和数量（取第一条明细）
            List<ProdFinishItem> items = finishItemMapper.selectList(
                    new LambdaQueryWrapper<ProdFinishItem>().eq(ProdFinishItem::getFinishId, f.getId()));
            if (!items.isEmpty()) {
                ProdFinishItem first = items.get(0);
                Material m = materialMapper.selectById(first.getMaterialId());
                f.setMaterialName(m != null ? m.getName() : "");
                f.setQuantity(first.getQuantity());
            }
        }
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    /** 详情（含明细），字段对齐前端表单 */
    @SaCheckPermission("production:order:view")
    @GetMapping("/{id}")
    public ApiResponse<java.util.Map<String, Object>> detail(@PathVariable Long id) {
        ProdFinish finish = finishMapper.selectById(id);
        if (finish == null) throw new BusinessException("完工入库单不存在");
        List<ProdFinishItem> items = finishItemMapper.selectList(
                new LambdaQueryWrapper<ProdFinishItem>().eq(ProdFinishItem::getFinishId, id));

        // 批量查物料
        final java.util.Map<Long, Material> matMap;
        if (!items.isEmpty()) {
            List<Long> mIds = items.stream().map(ProdFinishItem::getMaterialId).distinct().toList();
            matMap = materialMapper.selectBatchIds(mIds).stream()
                    .collect(java.util.stream.Collectors.toMap(Material::getId, m -> m, (a, b) -> a));
        } else {
            matMap = java.util.Collections.emptyMap();
        }

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", finish.getId());
        result.put("finishingNo", finish.getFinishNo());   // 前端字段名（含 ing）
        result.put("orderId", finish.getOrderId());
        if (finish.getOrderId() != null) {
            ProdOrder order = orderMapper.selectById(finish.getOrderId());
            result.put("orderNo", order != null ? order.getOrderNo() : "");
            result.put("planQuantity", order != null ? order.getPlanQuantity() : BigDecimal.ZERO);
            result.put("finishedQuantity", order != null ? order.getFinishQuantity() : BigDecimal.ZERO);
        } else {
            result.put("planQuantity", BigDecimal.ZERO);
            result.put("finishedQuantity", BigDecimal.ZERO);
        }
        result.put("warehouseId", finish.getWarehouseId());
        result.put("finishingDate", finish.getFinishDate() != null ? finish.getFinishDate().toString() : "");
        result.put("status", finish.getStatus());
        result.put("lines", items.stream().map(i -> {
            Material m = matMap.get(i.getMaterialId());
            java.util.Map<String, Object> line = new java.util.HashMap<>();
            line.put("id", i.getId());
            line.put("materialId", i.getMaterialId());
            line.put("materialName", m != null ? m.getName() : "");
            line.put("materialCode", m != null ? m.getCode() : "");
            line.put("unit", m != null ? m.getUnit() : "");
            line.put("quantity", i.getQuantity());
            line.put("finishQuantity", i.getQuantity());
            line.put("finishingQuantity", i.getQuantity());
            return line;
        }).toList());
        return ApiResponse.ok(result);
    }

    @SaCheckPermission("production:order:create")
    @PostMapping
    public ApiResponse<ProdFinish> create(@RequestBody java.util.Map<String, Object> body) {
        ProdFinish finish = mapToFinish(body);
        finish.setStatus("DRAFT");
        if (finish.getFinishNo() == null || finish.getFinishNo().isBlank()) {
            finish.setFinishNo(CodeGenerator.generate("FI", () -> {
                ProdFinish last = finishMapper.selectOne(
                        new LambdaQueryWrapper<ProdFinish>()
                                .select(ProdFinish::getFinishNo)
                                .orderByDesc(ProdFinish::getFinishNo)
                                .last("LIMIT 1"));
                return last != null ? last.getFinishNo() : null;
            }));
        }
        finishMapper.insert(finish);
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> lines = (java.util.List<java.util.Map<String, Object>>) body.get("lines");
        if (lines != null) saveFinishItems(finish.getId(), lines);
        return ApiResponse.ok(finish);
    }

    @SaCheckPermission("production:order:edit")
    @PutMapping("/{id}")
    public ApiResponse<ProdFinish> update(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        ProdFinish exist = finishMapper.selectById(id);
        if (exist == null) throw new BusinessException("完工入库单不存在");
        if (!"DRAFT".equals(exist.getStatus())) throw new BusinessException("仅草稿状态可修改");
        ProdFinish finish = mapToFinish(body);
        finish.setId(id);
        finishMapper.updateById(finish);
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> lines = (java.util.List<java.util.Map<String, Object>>) body.get("lines");
        if (lines != null) {
            finishItemMapper.delete(new LambdaQueryWrapper<ProdFinishItem>().eq(ProdFinishItem::getFinishId, id));
            saveFinishItems(id, lines);
        }
        return ApiResponse.ok(finishMapper.selectById(id));
    }

    // ==================== 业务操作 ====================

    /**
     * 确认完工入库：DRAFT → CONFIRMED，增加库存，记录流水
     */
    @SaCheckPermission("production:finish:confirm")
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

            // 增加库存（统一由 InvStockService 处理）
            stockService.receive(item.getMaterialId(), finish.getWarehouseId(),
                    item.getQuantity(), finish.getFinishNo(), "FINISH_IN");
        }

        // 更新工单完工数量（原子操作，防止并发覆盖）
        BigDecimal totalFinishQty = items.stream()
                .map(ProdFinishItem::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orderMapper.update(null,
                new LambdaUpdateWrapper<ProdOrder>()
                        .eq(ProdOrder::getId, finish.getOrderId())
                        .setSql("finish_quantity = COALESCE(finish_quantity, 0) + " + totalFinishQty));

        finish.setStatus("CONFIRMED");
        finishMapper.updateById(finish);

        log.info("完工入库单 {} 已确认，{} 条明细", finish.getFinishNo(), items.size());
        return ApiResponse.ok("确认成功");
    }

    /**
     * 删除（仅草稿）
     */
    @SaCheckPermission("production:order:delete")
    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        ProdFinish f = finishMapper.selectById(id);
        if (f == null) throw new BusinessException("完工入库单不存在");
        if (!"DRAFT".equals(f.getStatus())) throw new BusinessException("只有草稿状态可删除");
        finishItemMapper.delete(new LambdaQueryWrapper<ProdFinishItem>().eq(ProdFinishItem::getFinishId, id));
        finishMapper.deleteById(id);
        return ApiResponse.ok();
    }

    private ProdFinish mapToFinish(java.util.Map<String, Object> body) {
        ProdFinish f = new ProdFinish();
        if (body.containsKey("id")) f.setId(Long.valueOf(body.get("id").toString()));
        if (body.containsKey("orderId")) f.setOrderId(Long.valueOf(body.get("orderId").toString()));
        if (body.containsKey("warehouseId")) f.setWarehouseId(Long.valueOf(body.get("warehouseId").toString()));
        // 兼容 finishDate（DB字段名）和 finishingDate（前端字段名）
        if (body.containsKey("finishDate") && body.get("finishDate") != null && !body.get("finishDate").toString().isBlank())
            f.setFinishDate(LocalDate.parse(body.get("finishDate").toString()));
        else if (body.containsKey("finishingDate") && body.get("finishingDate") != null && !body.get("finishingDate").toString().isBlank())
            f.setFinishDate(LocalDate.parse(body.get("finishingDate").toString()));
        if (body.containsKey("status")) f.setStatus((String) body.get("status"));
        return f;
    }

    private void saveFinishItems(Long finishId, java.util.List<java.util.Map<String, Object>> lines) {
        for (java.util.Map<String, Object> line : lines) {
            ProdFinishItem item = new ProdFinishItem();
            item.setFinishId(finishId);
            item.setMaterialId(Long.valueOf(line.get("materialId").toString()));
            item.setQuantity(new BigDecimal(
                    line.get("quantity") != null
                            ? line.get("quantity").toString()
                            : (line.get("finishQuantity") != null
                                    ? line.get("finishQuantity").toString() : "0")));
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            finishItemMapper.insert(item);
        }
    }
}
