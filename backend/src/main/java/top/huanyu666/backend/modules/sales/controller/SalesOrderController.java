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

import java.util.List;
import java.util.Map;

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

    /**
     * 分页列表
     */
    @SaCheckPermission("sales:order:list")
    @GetMapping
    public ApiResponse<PageResult<SalesOrder>> list(PageParam param) {
        Page<SalesOrder> page = orderMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()),
                new LambdaQueryWrapper<SalesOrder>().orderByDesc(SalesOrder::getCreateTime)
        );
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    /**
     * 详情（含明细）
     */
    @SaCheckPermission("sales:order:list")
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
     * 详情（含明细），返回 Map 包装
     */
    @SaCheckPermission("sales:order:list")
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
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("order", order);
        result.put("items", items);
        return ApiResponse.ok(result);
    }

    /**
     * 创建订单 + 明细
     */
    @SaCheckPermission("sales:order:create")
    @PostMapping
    public ApiResponse<SalesOrder> create(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        Map<String, Object> orderMap = (Map<String, Object>) body.get("order");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemList = (List<Map<String, Object>>) body.get("items");

        SalesOrder order = mapToOrder(orderMap);
        List<SalesOrderItem> items = itemList.stream()
                .map(this::mapToOrderItem)
                .toList();

        SalesOrder created = orderService.createOrder(order, items);
        return ApiResponse.ok(created);
    }

    /**
     * 修改（校验 DRAFT 状态）
     */
    @SaCheckPermission("sales:order:update")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody SalesOrder order) {
        SalesOrder existing = orderMapper.selectById(id);
        if (existing == null) {
            return ApiResponse.error("订单不存在");
        }
        if (!"DRAFT".equals(existing.getStatus())) {
            return ApiResponse.error("只有草稿状态的订单才能修改");
        }
        order.setId(id);
        orderMapper.updateById(order);
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
    @SaCheckPermission("sales:order:list")
    @GetMapping("/{id}/deliverable-items")
    public ApiResponse<List<SalesOrderItem>> deliverableItems(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getDeliverableItems(id));
    }

    /**
     * 下推发货单
     */
    @SaCheckPermission("sales:delivery:create")
    @PostMapping("/{id}/create-delivery")
    public ApiResponse<Map<String, Object>> createDelivery(@PathVariable Long id, @RequestBody SalesDelivery delivery) {
        List<SalesOrderItem> deliverableItems = orderService.getDeliverableItems(id);

        delivery.setOrderId(id);
        delivery.setStatus("DRAFT");
        deliveryMapper.insert(delivery);

        for (SalesOrderItem orderItem : deliverableItems) {
            SalesDeliveryItem deliveryItem = new SalesDeliveryItem();
            deliveryItem.setDeliveryId(delivery.getId());
            deliveryItem.setOrderItemId(orderItem.getId());
            deliveryItem.setMaterialId(orderItem.getMaterialId());
            deliveryItem.setQuantity(orderItem.getQuantity().subtract(
                    orderItem.getShippedQty() != null ? orderItem.getShippedQty() : java.math.BigDecimal.ZERO));
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
        if (map.containsKey("orderDate")) o.setOrderDate(java.time.LocalDate.parse((String) map.get("orderDate")));
        if (map.containsKey("deliveryDate") && map.get("deliveryDate") != null)
            o.setDeliveryDate(java.time.LocalDate.parse((String) map.get("deliveryDate")));
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
