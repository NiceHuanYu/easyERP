package top.huanyu666.backend.modules.purchase.controller;

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
import top.huanyu666.backend.modules.finance.service.FinPayableService;
import top.huanyu666.backend.modules.inventory.service.InvStockService;
import top.huanyu666.backend.modules.purchase.entity.*;
import top.huanyu666.backend.modules.purchase.mapper.*;
import top.huanyu666.backend.modules.base.entity.Supplier;
import top.huanyu666.backend.modules.base.mapper.SupplierMapper;
import top.huanyu666.backend.modules.base.mapper.MaterialMapper;
import top.huanyu666.backend.modules.base.entity.Material;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 收货单管理
 */
@RestController
@RequestMapping("/api/v1/purchase/receivings")
@RequiredArgsConstructor
@Slf4j
public class PurReceivingController {

    private final PurReceivingMapper receivingMapper;
    private final PurReceivingItemMapper receivingItemMapper;
    private final PurOrderMapper orderMapper;
    private final PurOrderItemMapper orderItemMapper;
    private final InvStockService stockService;
    private final FinPayableService payableService;
    private final SupplierMapper supplierMapper;
    private final MaterialMapper materialMapper;

    // ==================== 基础 CRUD ====================

    @SaCheckPermission("purchase:order:view")
    @GetMapping
    public ApiResponse<PageResult<PurReceiving>> list(PageParam param,
                                                       @RequestParam(required = false) Long orderId,
                                                       @RequestParam(required = false) String status) {
        LambdaQueryWrapper<PurReceiving> qw = new LambdaQueryWrapper<>();
        if (orderId != null) {
            qw.eq(PurReceiving::getOrderId, orderId);
        }
        if (status != null && !status.isBlank()) {
            qw.eq(PurReceiving::getStatus, status);
        }
        qw.orderByDesc(PurReceiving::getCreateTime);
        Page<PurReceiving> page = receivingMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        page.getRecords().forEach(r -> {
            if (r.getOrderId() != null) {
                PurOrder o = orderMapper.selectById(r.getOrderId());
                if (o != null) {
                    r.setOrderNo(o.getOrderNo());
                    if (o.getSupplierId() != null) {
                        Supplier s = supplierMapper.selectById(o.getSupplierId());
                        r.setSupplierName(s != null ? s.getName() : "");
                    }
                }
            }
        });
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    /** 详情（含明细），字段对齐前端 */
    @SaCheckPermission("purchase:order:view")
    @GetMapping("/{id}")
    public ApiResponse<java.util.Map<String, Object>> detail(@PathVariable Long id) {
        PurReceiving r = receivingMapper.selectById(id);
        if (r == null) throw new BusinessException("收货单不存在");
        List<PurReceivingItem> items = receivingItemMapper.selectList(
                new LambdaQueryWrapper<PurReceivingItem>().eq(PurReceivingItem::getReceivingId, id));
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", r.getId());
        result.put("receivingNo", r.getReceivingNo());
        result.put("orderId", r.getOrderId());
        result.put("warehouseId", r.getWarehouseId());
        result.put("receivingDate", r.getReceivingDate() != null ? r.getReceivingDate().toString() : "");
        result.put("status", r.getStatus());
        result.put("remark", r.getRemark());
        result.put("lines", items.stream().map(i -> {
            java.util.Map<String, Object> line = new java.util.HashMap<>();
            line.put("orderItemId", i.getOrderItemId());
            line.put("materialId", i.getMaterialId());
            line.put("quantity", i.getQuantity());   // 本次收货数量
            Material m = materialMapper.selectById(i.getMaterialId());
            line.put("materialName", m != null ? m.getName() : "");
            line.put("materialCode", m != null ? m.getCode() : "");
            line.put("unit", m != null ? m.getUnit() : "");
            // 关联采购订单行信息
            if (i.getOrderItemId() != null) {
                PurOrderItem oi = orderItemMapper.selectById(i.getOrderItemId());
                if (oi != null) {
                    line.put("orderQuantity", oi.getQuantity());       // 订单行原始数量
                    line.put("receivedQuantity", oi.getReceivedQty()); // 已收货总量
                    line.put("price", oi.getPrice());
                    line.put("amount", oi.getAmount());
                }
            }
            return line;
        }).toList());
        return ApiResponse.ok(result);
    }

    @SaCheckPermission("purchase:order:create")
    @PostMapping
    public ApiResponse<PurReceiving> create(@RequestBody java.util.Map<String, Object> body) {
        PurReceiving receiving = mapToReceiving(body);
        receiving.setStatus("DRAFT");
        if (receiving.getReceivingNo() == null || receiving.getReceivingNo().isBlank()) {
            receiving.setReceivingNo(CodeGenerator.generate("RCV", () -> {
                PurReceiving last = receivingMapper.selectOne(
                        new LambdaQueryWrapper<PurReceiving>()
                                .select(PurReceiving::getReceivingNo)
                                .orderByDesc(PurReceiving::getReceivingNo)
                                .last("LIMIT 1"));
                return last != null ? last.getReceivingNo() : null;
            }));
        }
        receivingMapper.insert(receiving);
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> lines = (java.util.List<java.util.Map<String, Object>>) body.get("lines");
        if (lines != null) saveReceivingItems(receiving.getId(), lines);
        return ApiResponse.ok(receiving);
    }

    @SaCheckPermission("purchase:order:edit")
    @PutMapping("/{id}")
    public ApiResponse<PurReceiving> update(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        PurReceiving exist = receivingMapper.selectById(id);
        if (exist == null) throw new BusinessException("收货单不存在");
        if (!"DRAFT".equals(exist.getStatus())) throw new BusinessException("仅草稿状态可修改");
        PurReceiving receiving = mapToReceiving(body);
        receiving.setId(id);
        receivingMapper.updateById(receiving);
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> lines = (java.util.List<java.util.Map<String, Object>>) body.get("lines");
        if (lines != null) {
            receivingItemMapper.delete(new LambdaQueryWrapper<PurReceivingItem>().eq(PurReceivingItem::getReceivingId, id));
            saveReceivingItems(id, lines);
        }
        return ApiResponse.ok(receivingMapper.selectById(id));
    }

    // ==================== 业务操作 ====================

    /**
     * 确认收货：DRAFT → CONFIRMED，增加库存，记录流水，创建应付
     */
    @SaCheckPermission("purchase:order:approve")
    @PostMapping("/confirm/{id}")
    @Transactional
    public ApiResponse<String> confirm(@PathVariable Long id) {
        PurReceiving receiving = receivingMapper.selectById(id);
        if (receiving == null) {
            throw new BusinessException("收货单不存在");
        }
        if (!"DRAFT".equals(receiving.getStatus())) {
            throw new BusinessException("仅草稿状态可确认");
        }

        // 查收货明细
        LambdaQueryWrapper<PurReceivingItem> itemQw = new LambdaQueryWrapper<>();
        itemQw.eq(PurReceivingItem::getReceivingId, id);
        List<PurReceivingItem> items = receivingItemMapper.selectList(itemQw);

        if (items.isEmpty()) {
            throw new BusinessException("收货单无明细");
        }

        PurOrder order = orderMapper.selectById(receiving.getOrderId());
        if (order == null) {
            throw new BusinessException("关联的采购订单不存在");
        }
        if (!"APPROVED".equals(order.getStatus())) {
            throw new BusinessException("采购订单未审核，无法确认收货");
        }
        BigDecimal totalPayable = BigDecimal.ZERO;

        for (PurReceivingItem item : items) {
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("收货明细数量必须大于0");
            }

            // 增加库存（统一由 InvStockService 处理）
            stockService.receive(item.getMaterialId(), receiving.getWarehouseId(),
                    item.getQuantity(), receiving.getReceivingNo(), "PURCHASE_IN");

            // 更新订单明细已收数量
            if (item.getOrderItemId() != null) {
                PurOrderItem orderItem = orderItemMapper.selectById(item.getOrderItemId());
                if (orderItem != null) {
                    BigDecimal currentReceived = orderItem.getReceivedQty() != null
                            ? orderItem.getReceivedQty() : BigDecimal.ZERO;
                    orderItem.setReceivedQty(currentReceived.add(item.getQuantity()));
                    orderItem.setUpdateTime(LocalDateTime.now());
                    orderItemMapper.updateById(orderItem);

                    // 计算应付金额
                    if (orderItem.getPrice() != null) {
                        totalPayable = totalPayable.add(
                                orderItem.getPrice().multiply(item.getQuantity()));
                    }
                }
            }
        }

        // 创建应付台账
        if (order != null && totalPayable.compareTo(BigDecimal.ZERO) > 0) {
            payableService.createFromReceiving(id, order.getSupplierId(), totalPayable);
        }

        receiving.setStatus("CONFIRMED");
        receivingMapper.updateById(receiving);

        log.info("收货单 {} 已确认，{} 条明细，应付金额 {}", receiving.getReceivingNo(), items.size(), totalPayable);
        return ApiResponse.ok("确认成功");
    }

    private PurReceiving mapToReceiving(java.util.Map<String, Object> body) {
        PurReceiving r = new PurReceiving();
        if (body.containsKey("id")) r.setId(Long.valueOf(body.get("id").toString()));
        if (body.containsKey("orderId")) r.setOrderId(Long.valueOf(body.get("orderId").toString()));
        if (body.containsKey("warehouseId")) r.setWarehouseId(Long.valueOf(body.get("warehouseId").toString()));
        if (body.containsKey("receivingDate") && body.get("receivingDate") != null && !body.get("receivingDate").toString().isBlank()) r.setReceivingDate(java.time.LocalDate.parse(body.get("receivingDate").toString()));
        if (body.containsKey("status")) r.setStatus((String) body.get("status"));
        if (body.containsKey("remark")) r.setRemark((String) body.get("remark"));
        return r;
    }

    private void saveReceivingItems(Long receivingId, java.util.List<java.util.Map<String, Object>> lines) {
        for (java.util.Map<String, Object> line : lines) {
            PurReceivingItem item = new PurReceivingItem();
            item.setReceivingId(receivingId);
            if (line.containsKey("orderItemId")) item.setOrderItemId(Long.valueOf(line.get("orderItemId").toString()));
            if (line.containsKey("materialId") && line.get("materialId") != null)
                item.setMaterialId(Long.valueOf(line.get("materialId").toString()));
            item.setQuantity(new BigDecimal(line.get("receivingQuantity") != null
                    ? line.get("receivingQuantity").toString() : "0"));
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            receivingItemMapper.insert(item);
        }
    }
}
