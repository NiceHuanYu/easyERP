package top.huanyu666.backend.modules.sales.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import top.huanyu666.backend.modules.sales.entity.SalesDelivery;
import top.huanyu666.backend.modules.sales.entity.SalesDeliveryItem;
import top.huanyu666.backend.modules.sales.entity.SalesOrder;
import top.huanyu666.backend.modules.sales.entity.SalesOrderItem;
import top.huanyu666.backend.modules.sales.mapper.SalesDeliveryItemMapper;
import top.huanyu666.backend.modules.sales.mapper.SalesDeliveryMapper;
import top.huanyu666.backend.modules.sales.mapper.SalesOrderItemMapper;
import top.huanyu666.backend.modules.sales.mapper.SalesOrderMapper;
import top.huanyu666.backend.modules.sales.service.SalesOrderService;

import top.huanyu666.backend.modules.base.mapper.MaterialMapper;
import top.huanyu666.backend.modules.sales.dto.DeliverableItem;
import top.huanyu666.backend.modules.base.entity.Material;

import java.util.*;

/**
 * 销售订单管理
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/sales/orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderMapper orderMapper;
    private final SalesOrderItemMapper orderItemMapper;
    private final SalesDeliveryMapper deliveryMapper;
    private final SalesDeliveryItemMapper deliveryItemMapper;
    private final SalesOrderService orderService;
    private final MaterialMapper materialMapper;

    /**
     * 分页列表
     */
    @SaCheckPermission("sales:order:view")
    @GetMapping
    public ApiResponse<PageResult<SalesOrder>> list(PageParam param,
                                                     @RequestParam(required = false) String status) {
        LambdaQueryWrapper<SalesOrder> qw = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            qw.eq(SalesOrder::getStatus, status);
        }
        qw.orderByDesc(SalesOrder::getCreateTime);
        Page<SalesOrder> page = orderMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    /**
     * 详情（含明细）
     */
    @SaCheckPermission("sales:order:view")
    @GetMapping("/{id}")
    public ApiResponse<SalesOrder> getById(@PathVariable Long id) {
        SalesOrder order = orderMapper.selectById(id);
        if (order != null) {
            List<SalesOrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<SalesOrderItem>()
                            .eq(SalesOrderItem::getOrderId, id)
                            .orderByAsc(SalesOrderItem::getLineNo)
            );
            // 将明细附加到 order（若需要返回，通过 Map 包装或扩展；这里返回 order + items 的组合）
            // 由于 SalesOrder 没有 items 字段，改用 Map 包装返回
        }
        return ApiResponse.ok(order);
    }

    /**
     * 详情（含明细），返回扁平结构匹配前端 OrderForm
     */
    @SaCheckPermission("sales:order:view")
    @GetMapping("/{id}/detail")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        SalesOrder order = orderMapper.selectById(id);
        if (order == null) {
            return ApiResponse.error("订单不存在");
        }
        List<SalesOrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<SalesOrderItem>()
                        .eq(SalesOrderItem::getOrderId, id)
                        .orderByAsc(SalesOrderItem::getLineNo)
        );
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("customerId", order.getCustomerId());
        result.put("orderDate", order.getOrderDate() != null ? order.getOrderDate().toString() : null);
        result.put("deliveryDate", order.getDeliveryDate() != null ? order.getDeliveryDate().toString() : null);
        result.put("remark", order.getRemark());
        result.put("lines", items.stream().map(item -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("materialId", item.getMaterialId());
            m.put("quantity", item.getQuantity());
            m.put("price", item.getPrice());
            m.put("amount", item.getAmount());
            // 附带 materialName
            if (item.getMaterialId() != null) {
                Material mat = materialMapper.selectById(item.getMaterialId());
                m.put("materialName", mat != null ? mat.getName() : "");
            } else {
                m.put("materialName", "");
            }
            return m;
        }).toList());
        return ApiResponse.ok(result);
    }

    /**
     * 创建订单 + 明细
     */
    @SaCheckPermission("sales:order:create")
    @PostMapping
    public ApiResponse<SalesOrder> create(@RequestBody Map<String, Object> body) {
        SalesOrder order = mapToOrder(body);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemList = (List<Map<String, Object>>) body.get("lines");
        List<SalesOrderItem> items = itemList != null
                ? itemList.stream().map(this::mapToOrderItem).toList()
                : List.of();

        SalesOrder created = orderService.createOrder(order, items);
        return ApiResponse.ok(created);
    }

    /**
     * 修改（校验 DRAFT 状态）
     */
    @SaCheckPermission("sales:order:edit")
    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SalesOrder existing = orderMapper.selectById(id);
        if (existing == null) {
            return ApiResponse.error("订单不存在");
        }
        if (!"DRAFT".equals(existing.getStatus())) {
            return ApiResponse.error("只有草稿状态的订单才能修改");
        }
        SalesOrder order = mapToOrder(body);
        order.setId(id);
        orderMapper.updateById(order);

        // 更新明细：删旧插新
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemList = (List<Map<String, Object>>) body.get("lines");
        if (itemList != null) {
            orderItemMapper.delete(new LambdaQueryWrapper<SalesOrderItem>().eq(SalesOrderItem::getOrderId, id));
            for (Map<String, Object> itemMap : itemList) {
                SalesOrderItem item = mapToOrderItem(itemMap);
                item.setOrderId(id);
                orderItemMapper.insert(item);
            }
        }
        return ApiResponse.ok();
    }

    /**
     * 提交
     */
    @SaCheckPermission("sales:order:submit")
    @PostMapping("/{id}/submit")
    public ApiResponse<Void> submit(@PathVariable Long id) {
        orderService.submit(id);
        return ApiResponse.ok();
    }

    /**
     * 审核
     */
    @SaCheckPermission("sales:order:approve")
    @PostMapping("/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable Long id) {
        orderService.approve(id);
        return ApiResponse.ok();
    }

    /**
     * 驳回
     */
    @SaCheckPermission("sales:order:approve")
    @PostMapping("/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable Long id) {
        orderService.reject(id);
        return ApiResponse.ok();
    }

    /**
     * 关闭
     */
    @SaCheckPermission("sales:order:approve")
    @PostMapping("/{id}/close")
    public ApiResponse<Void> close(@PathVariable Long id) {
        orderService.close(id);
        return ApiResponse.ok();
    }

    /**
     * 可发货明细
     */
    @SaCheckPermission("sales:order:view")
    @GetMapping("/{id}/deliverable-items")
    public ApiResponse<List<DeliverableItem>> deliverableItems(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getDeliverableItems(id));
    }

    /**
     * 下推发货单
     */
    @SaCheckPermission("delivery:order:create")
    @PostMapping("/{id}/create-delivery")
    public ApiResponse<Map<String, Object>> createDelivery(@PathVariable Long id, @RequestBody SalesDelivery delivery) {
        List<DeliverableItem> deliverableItems = orderService.getDeliverableItems(id);

        delivery.setOrderId(id);
        delivery.setStatus("DRAFT");
        deliveryMapper.insert(delivery);

        for (DeliverableItem di : deliverableItems) {
            SalesDeliveryItem deliveryItem = new SalesDeliveryItem();
            deliveryItem.setDeliveryId(delivery.getId());
            deliveryItem.setOrderItemId(di.getOrderItemId());
            deliveryItem.setMaterialId(di.getMaterialId());
            deliveryItem.setQuantity(di.getDeliverableQuantity());
            deliveryItemMapper.insert(deliveryItem);
        }

        log.info("下推发货单: orderId={}, deliveryId={}", id, delivery.getId());

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("deliveryId", delivery.getId());
        return ApiResponse.ok(result);
    }

    /**
     * 删除（仅草稿）
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("sales:order:delete")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        SalesOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        if (!"DRAFT".equals(order.getStatus())) throw new BusinessException("只有草稿状态可删除");
        orderItemMapper.delete(new LambdaQueryWrapper<SalesOrderItem>().eq(SalesOrderItem::getOrderId, id));
        orderMapper.deleteById(id);
        return ApiResponse.ok();
    }

    /**
     * 反审核
     */
    @PostMapping("/{id}/unapprove")
    @SaCheckPermission("sales:order:approve")
    @Transactional
    public ApiResponse<Void> unapprove(@PathVariable Long id) {
        SalesOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        if (!"APPROVED".equals(order.getStatus())) throw new BusinessException("只有已审核状态可反审核");
        order.setStatus("SUBMITTED");
        orderMapper.updateById(order);
        return ApiResponse.ok();
    }

    // ---- 简单的 Map → Entity 转换 ----

    private SalesOrder mapToOrder(Map<String, Object> map) {
        SalesOrder o = new SalesOrder();
        if (map.containsKey("orderNo")) o.setOrderNo((String) map.get("orderNo"));
        if (map.containsKey("customerId")) o.setCustomerId(toLong(map.get("customerId")));
        if (map.containsKey("orderDate")) {
            Object od = map.get("orderDate");
            if (od != null && !od.toString().isBlank())
                o.setOrderDate(java.time.LocalDate.parse(od.toString()));
        }
        if (map.containsKey("deliveryDate")) {
            Object dd = map.get("deliveryDate");
            if (dd != null && !dd.toString().isBlank())
                o.setDeliveryDate(java.time.LocalDate.parse(dd.toString()));
        }
        if (map.containsKey("remark")) o.setRemark((String) map.get("remark"));
        return o;
    }

    private SalesOrderItem mapToOrderItem(Map<String, Object> map) {
        SalesOrderItem item = new SalesOrderItem();
        if (map.containsKey("lineNo")) item.setLineNo((Integer) map.get("lineNo"));
        if (map.containsKey("materialId")) item.setMaterialId(toLong(map.get("materialId")));
        if (map.containsKey("quantity")) item.setQuantity(new java.math.BigDecimal(map.get("quantity").toString()));
        if (map.containsKey("unit")) item.setUnit((String) map.get("unit"));
        if (map.containsKey("price")) item.setPrice(new java.math.BigDecimal(map.get("price").toString()));
        if (map.containsKey("amount")) item.setAmount(new java.math.BigDecimal(map.get("amount").toString()));
        if (item.getShippedQty() == null) item.setShippedQty(java.math.BigDecimal.ZERO);
        return item;
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.longValue();
        return Long.valueOf(val.toString());
    }
}
