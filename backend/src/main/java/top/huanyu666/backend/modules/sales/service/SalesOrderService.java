package top.huanyu666.backend.modules.sales.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.huanyu666.backend.common.enums.DocumentStatus;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.common.utils.CodeGenerator;
import top.huanyu666.backend.modules.base.entity.Material;
import top.huanyu666.backend.modules.base.mapper.MaterialMapper;
import top.huanyu666.backend.modules.sales.dto.DeliverableItem;
import top.huanyu666.backend.modules.sales.entity.SalesOrder;
import top.huanyu666.backend.modules.sales.entity.SalesOrderItem;
import top.huanyu666.backend.modules.sales.mapper.SalesOrderItemMapper;
import top.huanyu666.backend.modules.sales.mapper.SalesOrderMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 销售订单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderService {

    private final SalesOrderMapper orderMapper;
    private final SalesOrderItemMapper orderItemMapper;
    private final MaterialMapper materialMapper;

    /**
     * 创建订单（含明细）
     */
    @Transactional
    public SalesOrder createOrder(SalesOrder order, List<SalesOrderItem> items) {
        // 计算 totalAmount
        BigDecimal totalAmount = items.stream()
                .map(item -> {
                    if (item.getAmount() != null) {
                        return item.getAmount();
                    }
                    BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
                    BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                    return qty.multiply(price);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        order.setStatus(DocumentStatus.DRAFT.getCode());

        // 自动生成订单号
        if (order.getOrderNo() == null || order.getOrderNo().isBlank()) {
            order.setOrderNo(CodeGenerator.generate("SO", () -> {
                SalesOrder last = orderMapper.selectOne(
                        new LambdaQueryWrapper<SalesOrder>()
                                .select(SalesOrder::getOrderNo)
                                .orderByDesc(SalesOrder::getOrderNo)
                                .last("LIMIT 1")
                );
                return last != null ? last.getOrderNo() : null;
            }));
        }

        orderMapper.insert(order);

        for (int i = 0; i < items.size(); i++) {
            SalesOrderItem item = items.get(i);
            item.setOrderId(order.getId());
            if (item.getLineNo() == null) item.setLineNo(i + 1);
            orderItemMapper.insert(item);
        }

        log.info("创建销售订单: orderId={}, orderNo={}, totalAmount={}", order.getId(), order.getOrderNo(), totalAmount);
        return order;
    }

    /**
     * 提交：DRAFT → SUBMITTED
     */
    @Transactional
    public void submit(Long id) {
        SalesOrder order = getOrder(id);
        if (!DocumentStatus.DRAFT.eq(order.getStatus())) {
            throw new BusinessException("只有草稿状态的订单才能提交");
        }
        order.setStatus(DocumentStatus.SUBMITTED.getCode());
        orderMapper.updateById(order);
        log.info("提交销售订单: orderId={}", id);
    }

    /**
     * 审核：SUBMITTED → APPROVED
     */
    @Transactional
    public void approve(Long id) {
        SalesOrder order = getOrder(id);
        if (!DocumentStatus.SUBMITTED.eq(order.getStatus())) {
            throw new BusinessException("只有已提交状态的订单才能审核");
        }
        order.setStatus(DocumentStatus.APPROVED.getCode());
        orderMapper.updateById(order);
        log.info("审核销售订单: orderId={}", id);
    }

    /**
     * 驳回：SUBMITTED → DRAFT
     */
    @Transactional
    public void reject(Long id) {
        SalesOrder order = getOrder(id);
        if (!DocumentStatus.SUBMITTED.eq(order.getStatus())) {
            throw new BusinessException("只有已提交状态的订单才能驳回");
        }
        order.setStatus(DocumentStatus.DRAFT.getCode());
        orderMapper.updateById(order);
        log.info("驳回销售订单: orderId={}", id);
    }

    /**
     * 关闭：APPROVED → CLOSED
     */
    @Transactional
    public void close(Long id) {
        SalesOrder order = getOrder(id);
        if (!DocumentStatus.APPROVED.eq(order.getStatus())) {
            throw new BusinessException("只有已审核状态的订单才能关闭");
        }
        order.setStatus(DocumentStatus.CLOSED.getCode());
        orderMapper.updateById(order);
        log.info("关闭销售订单: orderId={}", id);
    }

    /**
     * 查询可发货明细（含物料名、可发货数量）
     */
    public List<DeliverableItem> getDeliverableItems(Long orderId) {
        SalesOrder order = getOrder(orderId);
        if (!DocumentStatus.APPROVED.eq(order.getStatus())) {
            throw new BusinessException("只有已审核状态的订单才能查询可发货明细");
        }
        List<SalesOrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<SalesOrderItem>()
                        .eq(SalesOrderItem::getOrderId, orderId)
                        .apply("shipped_qty < quantity"));
        List<DeliverableItem> result = new ArrayList<>();
        for (SalesOrderItem item : items) {
            Material material = materialMapper.selectById(item.getMaterialId());
            BigDecimal shipped = item.getShippedQty() != null ? item.getShippedQty() : BigDecimal.ZERO;
            result.add(new DeliverableItem(
                    item.getId(),
                    item.getMaterialId(),
                    material != null ? material.getName() : "未知物料",
                    item.getQuantity(),
                    item.getQuantity().subtract(shipped),
                    item.getPrice(),
                    item.getUnit()));
        }
        return result;
    }

    private SalesOrder getOrder(Long id) {
        SalesOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("销售订单不存在: " + id);
        }
        return order;
    }
}
