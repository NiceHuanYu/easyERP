package top.huanyu666.backend.modules.sales.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.utils.CodeGenerator;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import top.huanyu666.backend.modules.finance.entity.FinReceivable;
import top.huanyu666.backend.modules.finance.service.FinReceivableService;
import top.huanyu666.backend.modules.inventory.service.InvStockService;
import top.huanyu666.backend.modules.sales.entity.*;
import top.huanyu666.backend.modules.sales.mapper.*;
import top.huanyu666.backend.modules.base.entity.Customer;
import top.huanyu666.backend.modules.base.mapper.CustomerMapper;
import top.huanyu666.backend.modules.base.entity.Material;
import top.huanyu666.backend.modules.base.mapper.MaterialMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售发货单管理
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/sales/deliveries")
@RequiredArgsConstructor
public class SalesDeliveryController {

    private final SalesDeliveryMapper deliveryMapper;
    private final SalesDeliveryItemMapper deliveryItemMapper;
    private final SalesOrderMapper orderMapper;
    private final SalesOrderItemMapper orderItemMapper;
    private final InvStockService invStockService;
    private final FinReceivableService finReceivableService;
    private final CustomerMapper customerMapper;
    private final MaterialMapper materialMapper;

    /**
     * 分页列表
     */
    @SaCheckPermission("delivery:order:view")
    @GetMapping
    public ApiResponse<PageResult<SalesDelivery>> list(PageParam param,
            @RequestParam(required = false) String deliveryNo,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<SalesDelivery> qw = new LambdaQueryWrapper<>();
        if (deliveryNo != null && !deliveryNo.isBlank()) qw.like(SalesDelivery::getDeliveryNo, deliveryNo);
        if (customerId != null) {
            // 按客户ID查询：先查该客户的所有订单ID，再按订单ID过滤发货单
            List<SalesOrder> customerOrders = orderMapper.selectList(
                    new LambdaQueryWrapper<SalesOrder>().eq(SalesOrder::getCustomerId, customerId)
            );
            List<Long> orderIds = customerOrders.stream().map(SalesOrder::getId).toList();
            if (orderIds.isEmpty()) {
                return ApiResponse.ok(new PageResult<>(0L, param.getPage(), param.getSize(), List.of()));
            }
            qw.in(SalesDelivery::getOrderId, orderIds);
        }
        if (status != null && !status.isBlank()) qw.eq(SalesDelivery::getStatus, status);
        qw.orderByDesc(SalesDelivery::getCreateTime);
        Page<SalesDelivery> page = deliveryMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        for (SalesDelivery d : page.getRecords()) {
            if (d.getOrderId() != null) {
                SalesOrder order = orderMapper.selectById(d.getOrderId());
                if (order != null) {
                    d.setOrderNo(order.getOrderNo());
                    if (order.getCustomerId() != null) {
                        Customer c = customerMapper.selectById(order.getCustomerId());
                        d.setCustomerName(c != null ? c.getName() : "");
                    }
                }
            }
        }
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    /**
     * 详情（含明细）
     */
    @SaCheckPermission("delivery:order:view")
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        SalesDelivery delivery = deliveryMapper.selectById(id);
        if (delivery == null) throw new BusinessException("发货单不存在");
        List<SalesDeliveryItem> items = deliveryItemMapper.selectList(
                new LambdaQueryWrapper<SalesDeliveryItem>().eq(SalesDeliveryItem::getDeliveryId, id));
        List<Map<String, Object>> lines = new ArrayList<>();
        if (delivery.getOrderId() != null) {
            SalesOrder order = orderMapper.selectById(delivery.getOrderId());
            List<SalesOrderItem> orderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<SalesOrderItem>().eq(SalesOrderItem::getOrderId, delivery.getOrderId()));
            Map<Long, SalesOrderItem> orderItemMap = orderItems.stream()
                    .collect(java.util.stream.Collectors.toMap(SalesOrderItem::getId, o -> o, (a, b) -> a));
            for (SalesDeliveryItem item : items) {
                Map<String, Object> line = new HashMap<>();
                line.put("id", item.getId());
                line.put("orderItemId", item.getOrderItemId());
                line.put("materialId", item.getMaterialId());
                line.put("deliveryQuantity", item.getQuantity());
                SalesOrderItem oi = orderItemMap.get(item.getOrderItemId());
                line.put("orderQuantity", oi != null ? oi.getQuantity() : BigDecimal.ZERO);
                BigDecimal shipped = oi != null && oi.getShippedQty() != null ? oi.getShippedQty() : BigDecimal.ZERO;
                line.put("deliverableQuantity", oi != null ? oi.getQuantity().subtract(shipped) : BigDecimal.ZERO);
                line.put("price", oi != null ? oi.getPrice() : BigDecimal.ZERO);
                line.put("subtotal", (item.getQuantity() != null && oi != null && oi.getPrice() != null)
                        ? item.getQuantity().multiply(oi.getPrice()) : BigDecimal.ZERO);
                if (item.getMaterialId() != null) {
                    Material m = materialMapper.selectById(item.getMaterialId());
                    line.put("materialName", m != null ? m.getName() : "");
                    line.put("unit", m != null ? m.getUnit() : "");
                }
                lines.add(line);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("id", delivery.getId());
        result.put("deliveryNo", delivery.getDeliveryNo());
        result.put("orderId", delivery.getOrderId());
        result.put("warehouseId", delivery.getWarehouseId());
        result.put("deliveryDate", delivery.getDeliveryDate() != null ? delivery.getDeliveryDate().toString() : "");
        result.put("status", delivery.getStatus());
        result.put("remark", delivery.getRemark() != null ? delivery.getRemark() : "");
        result.put("lines", lines);
        return ApiResponse.ok(result);
    }

    /**
     * 创建发货单 + 明细
     */
    @SaCheckPermission("delivery:order:create")
    @PostMapping
    public ApiResponse<SalesDelivery> create(@RequestBody Map<String, Object> body) {
        SalesDelivery delivery = mapToDelivery(body);
        delivery.setStatus("DRAFT");
        if (delivery.getDeliveryNo() == null || delivery.getDeliveryNo().isBlank()) {
            delivery.setDeliveryNo(CodeGenerator.generate("SD", () -> {
                SalesDelivery last = deliveryMapper.selectOne(
                        new LambdaQueryWrapper<SalesDelivery>()
                                .select(SalesDelivery::getDeliveryNo)
                                .orderByDesc(SalesDelivery::getDeliveryNo)
                                .last("LIMIT 1"));
                return last != null ? last.getDeliveryNo() : null;
            }));
        }
        deliveryMapper.insert(delivery);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lines = (List<Map<String, Object>>) body.get("lines");
        if (lines != null) {
            saveDeliveryItems(delivery.getId(), lines);
        }
        return ApiResponse.ok(delivery);
    }

    /**
     * 修改发货单 + 明细
     */
    @SaCheckPermission("delivery:order:create")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SalesDelivery existing = deliveryMapper.selectById(id);
        if (existing == null) throw new BusinessException("发货单不存在");
        if (!"DRAFT".equals(existing.getStatus())) throw new BusinessException("只有草稿状态的发货单才能修改");

        SalesDelivery delivery = mapToDelivery(body);
        delivery.setId(id);
        deliveryMapper.updateById(delivery);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lines = (List<Map<String, Object>>) body.get("lines");
        if (lines != null) {
            deliveryItemMapper.delete(new LambdaQueryWrapper<SalesDeliveryItem>().eq(SalesDeliveryItem::getDeliveryId, id));
            saveDeliveryItems(id, lines);
        }
        return ApiResponse.ok();
    }

    /**
     * 确认发货
     */
    @SaCheckPermission("delivery:order:approve")
    @PostMapping("/{id}/confirm")
    @Transactional
    public ApiResponse<Void> confirm(@PathVariable Long id) {
        SalesDelivery delivery = deliveryMapper.selectById(id);
        if (delivery == null) {
            throw new BusinessException("发货单不存在: " + id);
        }
        if (!"DRAFT".equals(delivery.getStatus())) {
            throw new BusinessException("只有草稿状态的发货单才能确认发货");
        }

        // 1. 获取发货明细
        List<SalesDeliveryItem> deliveryItems = deliveryItemMapper.selectList(
                new LambdaQueryWrapper<SalesDeliveryItem>().eq(SalesDeliveryItem::getDeliveryId, id)
        );

        if (deliveryItems.isEmpty()) {
            throw new BusinessException("发货单没有明细，无法确认发货");
        }

        // 获取订单
        SalesOrder order = orderMapper.selectById(delivery.getOrderId());
        if (order == null) {
            throw new BusinessException("关联的销售订单不存在");
        }
        if (!"APPROVED".equals(order.getStatus())) {
            throw new BusinessException("销售订单未审核，无法发货: " + order.getOrderNo());
        }

        // 计算本次发货金额并扣减库存
        BigDecimal deliveryAmount = BigDecimal.ZERO;

        for (SalesDeliveryItem deliveryItem : deliveryItems) {
            // 2. 更新订单明细的 shippedQty
            SalesOrderItem orderItem = orderItemMapper.selectById(deliveryItem.getOrderItemId());
            if (orderItem == null) {
                throw new BusinessException("订单明细不存在: " + deliveryItem.getOrderItemId());
            }

            BigDecimal newShippedQty = orderItem.getShippedQty() != null
                    ? orderItem.getShippedQty().add(deliveryItem.getQuantity())
                    : deliveryItem.getQuantity();

            if (newShippedQty.compareTo(orderItem.getQuantity()) > 0) {
                throw new BusinessException("发货数量超过订单数量: orderItemId=" + orderItem.getId());
            }

            orderItem.setShippedQty(newShippedQty);
            orderItem.setUpdateTime(LocalDateTime.now());
            orderItemMapper.updateById(orderItem);

            // 累计本次发货金额
            if (orderItem.getPrice() != null) {
                deliveryAmount = deliveryAmount.add(
                        orderItem.getPrice().multiply(deliveryItem.getQuantity()));
            }

            // 3. 扣减库存（统一由 InvStockService 处理）
            invStockService.deduct(deliveryItem.getMaterialId(), delivery.getWarehouseId(),
                    deliveryItem.getQuantity(), delivery.getDeliveryNo(), "SALES_OUT");
        }

        // 5. 检查订单是否全部发货，更新订单状态
        List<SalesOrderItem> allItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<SalesOrderItem>().eq(SalesOrderItem::getOrderId, order.getId())
        );

        boolean allShipped = allItems.stream()
                .allMatch(item -> item.getShippedQty() != null
                        && item.getShippedQty().compareTo(item.getQuantity()) >= 0);

        if (allShipped) {
            order.setStatus("SHIPPED");
            orderMapper.updateById(order);
            log.info("订单全部发货: orderId={}", order.getId());
        }

        // 6. 更新发货单状态
        delivery.setStatus("CONFIRMED");
        deliveryMapper.updateById(delivery);

        // 7. 创建应收台账（防重复）
        FinReceivable existReceivable = finReceivableService.findByDeliveryId(delivery.getId());
        if (existReceivable != null) {
            throw new BusinessException("该发货单已创建应收台账，请勿重复确认");
        }
        finReceivableService.createFromDelivery(delivery.getId(), order.getCustomerId(), deliveryAmount);

        log.info("确认发货成功: deliveryId={}, orderId={}", id, order.getId());
        return ApiResponse.ok();
    }

    /**
     * 删除发货单（仅草稿）
     */
    @SaCheckPermission("delivery:order:delete")
    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        SalesDelivery delivery = deliveryMapper.selectById(id);
        if (delivery == null) throw new BusinessException("发货单不存在");
        if (!"DRAFT".equals(delivery.getStatus())) throw new BusinessException("只有草稿状态可删除");
        deliveryItemMapper.delete(new LambdaQueryWrapper<SalesDeliveryItem>().eq(SalesDeliveryItem::getDeliveryId, id));
        deliveryMapper.deleteById(id);
        return ApiResponse.ok();
    }

    // ---- helpers ----

    @SuppressWarnings("unchecked")
    private SalesDelivery mapToDelivery(Map<String, Object> body) {
        SalesDelivery d = new SalesDelivery();
        if (body.containsKey("id")) d.setId(Long.valueOf(body.get("id").toString()));
        if (body.containsKey("deliveryNo")) d.setDeliveryNo((String) body.get("deliveryNo"));
        if (body.containsKey("orderId")) d.setOrderId(Long.valueOf(body.get("orderId").toString()));
        if (body.containsKey("warehouseId")) d.setWarehouseId(Long.valueOf(body.get("warehouseId").toString()));
        if (body.containsKey("deliveryDate") && body.get("deliveryDate") != null && !body.get("deliveryDate").toString().isBlank()) d.setDeliveryDate(LocalDate.parse(body.get("deliveryDate").toString()));
        if (body.containsKey("status")) d.setStatus((String) body.get("status"));
        if (body.containsKey("remark")) d.setRemark((String) body.get("remark"));
        return d;
    }

    private void saveDeliveryItems(Long deliveryId, List<Map<String, Object>> lines) {
        for (Map<String, Object> line : lines) {
            SalesDeliveryItem item = new SalesDeliveryItem();
            item.setDeliveryId(deliveryId);
            if (line.containsKey("id")) item.setId(Long.valueOf(line.get("id").toString()));
            if (line.containsKey("orderItemId") && line.get("orderItemId") != null)
                item.setOrderItemId(Long.valueOf(line.get("orderItemId").toString()));
            if (line.containsKey("materialId") && line.get("materialId") != null)
                item.setMaterialId(Long.valueOf(line.get("materialId").toString()));
            if (line.containsKey("deliveryQuantity") && line.get("deliveryQuantity") != null)
                item.setQuantity(new BigDecimal(line.get("deliveryQuantity").toString()));
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            deliveryItemMapper.insert(item);
        }
    }
}
