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
import top.huanyu666.backend.modules.purchase.entity.*;
import top.huanyu666.backend.modules.purchase.mapper.*;
import top.huanyu666.backend.modules.purchase.service.PurOrderService;
import top.huanyu666.backend.modules.base.entity.Supplier;
import top.huanyu666.backend.modules.base.mapper.MaterialMapper;
import top.huanyu666.backend.modules.base.mapper.SupplierMapper;
import top.huanyu666.backend.modules.base.entity.Material;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 采购订单管理
 */
@RestController
@RequestMapping("/api/v1/purchase/orders")
@RequiredArgsConstructor
@Slf4j
public class PurOrderController {

    private final PurOrderMapper orderMapper;
    private final PurOrderItemMapper orderItemMapper;
    private final PurOrderService orderService;
    private final SupplierMapper supplierMapper;
    private final MaterialMapper materialMapper;

    // ==================== 基础 CRUD ====================

    @SaCheckPermission("purchase:order:view")
    @GetMapping
    public ApiResponse<PageResult<PurOrder>> list(PageParam param,
                                                   @RequestParam(required = false) String orderNo,
                                                   @RequestParam(required = false) Long supplierId,
                                                   @RequestParam(required = false) String status) {
        LambdaQueryWrapper<PurOrder> qw = new LambdaQueryWrapper<>();
        if (orderNo != null && !orderNo.isBlank()) qw.like(PurOrder::getOrderNo, orderNo);
        if (supplierId != null) {
            qw.eq(PurOrder::getSupplierId, supplierId);
        }
        if (status != null && !status.isBlank()) {
            qw.eq(PurOrder::getStatus, status);
        }
        qw.orderByDesc(PurOrder::getCreateTime);
        Page<PurOrder> page = orderMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        page.getRecords().forEach(o -> {
            if (o.getSupplierId() != null) {
                Supplier s = supplierMapper.selectById(o.getSupplierId());
                o.setSupplierName(s != null ? s.getName() : "");
            }
        });
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    /** 详情（含明细） */
    @SaCheckPermission("purchase:order:view")
    @GetMapping("/{id}")
    public ApiResponse<java.util.Map<String, Object>> detail(@PathVariable Long id) {
        PurOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("采购订单不存在");
        List<PurOrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<PurOrderItem>().eq(PurOrderItem::getOrderId, id));
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("order", order);
        result.put("lines", items.stream().map(i -> {
            java.util.Map<String, Object> line = new java.util.HashMap<>();
            line.put("id", i.getId());
            line.put("materialId", i.getMaterialId());
            line.put("quantity", i.getQuantity());
            line.put("price", i.getPrice());
            line.put("amount", i.getAmount());
            line.put("receivedQty", i.getReceivedQty());
            Material m = materialMapper.selectById(i.getMaterialId());
            line.put("materialName", m != null ? m.getName() : "");
            return line;
        }).toList());
        return ApiResponse.ok(result);
    }

    @SaCheckPermission("purchase:order:create")
    @PostMapping
    public ApiResponse<PurOrder> create(@RequestBody java.util.Map<String, Object> body) {
        PurOrder order = mapToOrder(body);
        order.setStatus("DRAFT");
        if (order.getOrderNo() == null || order.getOrderNo().isBlank()) {
            order.setOrderNo(CodeGenerator.generate("PO", () -> {
                PurOrder last = orderMapper.selectOne(
                        new LambdaQueryWrapper<PurOrder>()
                                .select(PurOrder::getOrderNo)
                                .orderByDesc(PurOrder::getOrderNo)
                                .last("LIMIT 1"));
                return last != null ? last.getOrderNo() : null;
            }));
        }
        orderMapper.insert(order);
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> lines = (java.util.List<java.util.Map<String, Object>>) body.get("lines");
        if (lines != null) saveOrderItems(order.getId(), lines);
        return ApiResponse.ok(order);
    }

    // ==================== 业务操作 ====================

    @SaCheckPermission("purchase:order:create")
    @PostMapping("/create-receiving/{id}")
    @Transactional
    public ApiResponse<PurReceiving> createReceiving(@PathVariable Long id,
                                                      @RequestBody PurReceiving receiving) {
        return ApiResponse.ok(orderService.createReceiving(id, receiving));
    }

    // ==================== 编辑/删除/下达 ====================

    @SaCheckPermission("purchase:order:create")
    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        PurOrder exist = orderMapper.selectById(id);
        if (exist == null) throw new BusinessException("采购订单不存在");
        if (!"DRAFT".equals(exist.getStatus())) throw new BusinessException("只有草稿状态可编辑");
        PurOrder order = mapToOrder(body);
        order.setId(id);
        orderMapper.updateById(order);
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> lines = (java.util.List<java.util.Map<String, Object>>) body.get("lines");
        if (lines != null) {
            orderItemMapper.delete(new LambdaQueryWrapper<PurOrderItem>().eq(PurOrderItem::getOrderId, id));
            saveOrderItems(id, lines);
        }
        return ApiResponse.ok();
    }

    @SaCheckPermission("purchase:order:create")
    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ApiResponse.ok();
    }

    @SaCheckPermission("purchase:order:create")
    @PostMapping("/{id}/issue")
    @Transactional
    public ApiResponse<Void> issue(@PathVariable Long id) {
        orderService.issue(id);
        return ApiResponse.ok();
    }

    private PurOrder mapToOrder(java.util.Map<String, Object> body) {
        PurOrder o = new PurOrder();
        if (body.containsKey("supplierId")) o.setSupplierId(Long.valueOf(body.get("supplierId").toString()));
        if (body.containsKey("orderDate") && body.get("orderDate") != null && !body.get("orderDate").toString().isBlank()) o.setOrderDate(LocalDate.parse(body.get("orderDate").toString()));
        if (body.containsKey("remark")) o.setRemark((String) body.get("remark"));
        if (body.containsKey("totalAmount") && body.get("totalAmount") != null) o.setTotalAmount(new BigDecimal(body.get("totalAmount").toString()));
        if (body.containsKey("requisitionId") && body.get("requisitionId") != null) o.setRequisitionId(Long.valueOf(body.get("requisitionId").toString()));
        if (body.containsKey("deliveryDate") && body.get("deliveryDate") != null && !body.get("deliveryDate").toString().isBlank())
            o.setDeliveryDate(LocalDate.parse(body.get("deliveryDate").toString()));
        return o;
    }

    private void saveOrderItems(Long orderId, java.util.List<java.util.Map<String, Object>> lines) {
        int lineNo = 1;
        for (java.util.Map<String, Object> line : lines) {
            PurOrderItem item = new PurOrderItem();
            item.setOrderId(orderId);
            item.setLineNo(lineNo++);
            if (line.containsKey("materialId") && line.get("materialId") != null)
                item.setMaterialId(Long.valueOf(line.get("materialId").toString()));
            if (line.containsKey("quantity") && line.get("quantity") != null)
                item.setQuantity(new BigDecimal(line.get("quantity").toString()));
            item.setReceivedQty(BigDecimal.ZERO);
            item.setPrice(new BigDecimal(line.get("price") != null ? line.get("price").toString() : "0"));
            item.setAmount(new BigDecimal(line.get("amount") != null ? line.get("amount").toString() : "0"));
            item.setUnit("");
            orderItemMapper.insert(item);
        }
    }
}
