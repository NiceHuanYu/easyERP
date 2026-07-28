package top.huanyu666.backend.modules.sales.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import top.huanyu666.backend.modules.finance.entity.FinReceivable;
import top.huanyu666.backend.modules.inventory.service.InvStockService;
import top.huanyu666.backend.modules.sales.entity.*;
import top.huanyu666.backend.modules.sales.mapper.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    // FinReceivable 没有 Mapper，通过 BaseMapper 注入；此处声明类型引用，
    // 实际使用时通过 com.baomidou.mybatisplus.core.mapper.BaseMapper<FinReceivable> 操作
    private final com.baomidou.mybatisplus.core.mapper.BaseMapper<FinReceivable> finReceivableMapper;

    /**
     * 分页列表
     */
    @SaCheckPermission("delivery:order:view")
    @GetMapping
    public ApiResponse<PageResult<SalesDelivery>> list(PageParam param) {
        Page<SalesDelivery> page = deliveryMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()),
                new LambdaQueryWrapper<SalesDelivery>().orderByDesc(SalesDelivery::getCreateTime)
        );
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    /**
     * 创建发货单
     */
    @SaCheckPermission("delivery:order:create")
    @PostMapping
    public ApiResponse<SalesDelivery> create(@RequestBody SalesDelivery delivery) {
        delivery.setStatus("DRAFT");
        deliveryMapper.insert(delivery);
        return ApiResponse.ok(delivery);
    }

    /**
     * 修改发货单
     */
    @SaCheckPermission("delivery:order:create")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody SalesDelivery delivery) {
        SalesDelivery existing = deliveryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("发货单不存在");
        }
        if (!"DRAFT".equals(existing.getStatus())) {
            throw new BusinessException("只有草稿状态的发货单才能修改");
        }
        delivery.setId(id);
        deliveryMapper.updateById(delivery);
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

        // 7. 创建应收台账
        FinReceivable receivable = new FinReceivable();
        receivable.setDeliveryId(delivery.getId());
        receivable.setCustomerId(order.getCustomerId());
        receivable.setReceivableAmount(order.getTotalAmount());
        receivable.setReceivedAmount(BigDecimal.ZERO);
        receivable.setStatus("PENDING");
        finReceivableMapper.insert(receivable);

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
}
