package top.huanyu666.backend.modules.purchase.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.huanyu666.backend.common.enums.DocumentStatus;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.modules.purchase.entity.*;
import top.huanyu666.backend.modules.purchase.mapper.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购订单服务 —— 封装收货单创建、下达等业务逻辑。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PurOrderService {

    private final PurOrderMapper orderMapper;
    private final PurOrderItemMapper orderItemMapper;
    private final PurReceivingMapper receivingMapper;
    private final PurReceivingItemMapper receivingItemMapper;

    /**
     * 根据采购订单创建收货单：查未收完的明细 → 创建收货单主表 + 明细。
     */
    @Transactional
    public PurReceiving createReceiving(Long orderId, PurReceiving receiving) {
        PurOrder order = getOrder(orderId);

        LambdaQueryWrapper<PurOrderItem> itemQw = new LambdaQueryWrapper<>();
        itemQw.eq(PurOrderItem::getOrderId, orderId);
        itemQw.apply("quantity - received_qty > 0");
        List<PurOrderItem> orderItems = orderItemMapper.selectList(itemQw);

        if (orderItems.isEmpty()) {
            throw new BusinessException("该订单没有待收货明细");
        }

        receiving.setOrderId(orderId);
        receiving.setStatus(DocumentStatus.DRAFT.getCode());
        receiving.setReceivingDate(LocalDate.now());
        receivingMapper.insert(receiving);

        for (PurOrderItem orderItem : orderItems) {
            BigDecimal received = orderItem.getReceivedQty() != null
                    ? orderItem.getReceivedQty() : BigDecimal.ZERO;
            BigDecimal remaining = orderItem.getQuantity().subtract(received);

            PurReceivingItem item = new PurReceivingItem();
            item.setReceivingId(receiving.getId());
            item.setOrderItemId(orderItem.getId());
            item.setMaterialId(orderItem.getMaterialId());
            item.setQuantity(remaining);
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            receivingItemMapper.insert(item);
        }

        log.info("采购订单 {} 创建收货单 {}，{} 条明细", order.getOrderNo(), receiving.getReceivingNo(), orderItems.size());
        return receiving;
    }

    /**
     * 下达采购订单 → APPROVED。
     */
    @Transactional
    public void issue(Long id) {
        PurOrder order = getOrder(id);
        if (!DocumentStatus.DRAFT.eq(order.getStatus()) && !DocumentStatus.SUBMITTED.eq(order.getStatus())) {
            throw new BusinessException("只有草稿或已提交状态可下达");
        }
        order.setStatus(DocumentStatus.APPROVED.getCode());
        orderMapper.updateById(order);
        log.info("采购订单 {} 已下达", order.getOrderNo());
    }

    /**
     * 删除（仅草稿）—— 同时删除明细。
     */
    @Transactional
    public void delete(Long id) {
        PurOrder order = getOrder(id);
        if (!DocumentStatus.DRAFT.eq(order.getStatus())) {
            throw new BusinessException("只有草稿状态可删除");
        }
        orderItemMapper.delete(new LambdaQueryWrapper<PurOrderItem>().eq(PurOrderItem::getOrderId, id));
        orderMapper.deleteById(id);
        log.info("删除采购订单: orderId={}, orderNo={}", id, order.getOrderNo());
    }

    private PurOrder getOrder(Long id) {
        PurOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }
        return order;
    }
}
