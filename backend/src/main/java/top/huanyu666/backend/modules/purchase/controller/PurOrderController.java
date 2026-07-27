package top.huanyu666.backend.modules.purchase.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import top.huanyu666.backend.modules.purchase.entity.*;
import top.huanyu666.backend.modules.purchase.mapper.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    private final PurReceivingMapper receivingMapper;
    private final PurReceivingItemMapper receivingItemMapper;

    // ==================== 基础 CRUD ====================

    @GetMapping
    public ApiResponse<PageResult<PurOrder>> list(PageParam param,
                                                   @RequestParam(required = false) Long supplierId,
                                                   @RequestParam(required = false) String status) {
        LambdaQueryWrapper<PurOrder> qw = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            qw.eq(PurOrder::getSupplierId, supplierId);
        }
        if (status != null && !status.isBlank()) {
            qw.eq(PurOrder::getStatus, status);
        }
        qw.orderByDesc(PurOrder::getCreateTime);
        Page<PurOrder> page = orderMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @GetMapping("/{id}")
    public ApiResponse<PurOrder> detail(@PathVariable Long id) {
        PurOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }
        return ApiResponse.ok(order);
    }

    @PostMapping
    public ApiResponse<PurOrder> create(@RequestBody PurOrder order) {
        order.setStatus("DRAFT");
        orderMapper.insert(order);
        return ApiResponse.ok(order);
    }

    // ==================== 业务操作 ====================

    /**
     * 生成收货单
     */
    @PostMapping("/create-receiving/{id}")
    @Transactional
    public ApiResponse<PurReceiving> createReceiving(@PathVariable Long id,
                                                      @RequestBody PurReceiving receiving) {
        PurOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }

        // 查订单明细（未收完的）
        LambdaQueryWrapper<PurOrderItem> itemQw = new LambdaQueryWrapper<>();
        itemQw.eq(PurOrderItem::getOrderId, id);
        itemQw.apply("quantity - received_qty > 0");
        List<PurOrderItem> orderItems = orderItemMapper.selectList(itemQw);

        if (orderItems.isEmpty()) {
            throw new BusinessException("该订单没有待收货明细");
        }

        // 创建收货单
        receiving.setOrderId(id);
        receiving.setStatus("DRAFT");
        receiving.setReceivingDate(LocalDate.now());
        receivingMapper.insert(receiving);

        // 创建收货明细
        for (PurOrderItem orderItem : orderItems) {
            BigDecimal remaining = orderItem.getQuantity().subtract(
                    orderItem.getReceivedQty() != null ? orderItem.getReceivedQty() : BigDecimal.ZERO);
            PurReceivingItem item = new PurReceivingItem();
            item.setReceivingId(receiving.getId());
            item.setOrderItemId(orderItem.getId());
            item.setMaterialId(orderItem.getMaterialId());
            item.setQuantity(remaining);
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            receivingItemMapper.insert(item);
        }

        log.info("采购订单 {} 创建收货单 {}", order.getOrderNo(), receiving.getReceivingNo());
        return ApiResponse.ok(receiving);
    }
}
