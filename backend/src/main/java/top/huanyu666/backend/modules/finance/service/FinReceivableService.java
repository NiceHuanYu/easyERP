package top.huanyu666.backend.modules.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.huanyu666.backend.common.enums.DocumentStatus;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.modules.finance.entity.FinReceivable;
import top.huanyu666.backend.modules.finance.mapper.FinReceivableMapper;

import java.math.BigDecimal;

/**
 * 应收台账服务。
 * <p>
 * 调用方负责 {@code @Transactional} 事务边界。
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FinReceivableService {

    private final FinReceivableMapper receivableMapper;

    /**
     * 发货确认时创建应收台账。
     */
    public FinReceivable createFromDelivery(Long deliveryId, Long customerId, BigDecimal amount) {
        // 防重复创建
        FinReceivable exist = findByDeliveryId(deliveryId);
        if (exist != null) {
            throw new BusinessException("该发货单已创建应收台账");
        }
        FinReceivable receivable = new FinReceivable();
        receivable.setDeliveryId(deliveryId);
        receivable.setCustomerId(customerId);
        receivable.setReceivableAmount(amount);
        receivable.setReceivedAmount(BigDecimal.ZERO);
        receivable.setStatus(DocumentStatus.PENDING.getCode());
        receivableMapper.insert(receivable);
        log.info("创建应收台账: deliveryId={}, customerId={}, amount={}", deliveryId, customerId, amount);
        return receivable;
    }

    /**
     * 收款核销 —— 增加已收金额，自动更新状态。
     */
    public FinReceivable applyPayment(Long receivableId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("核销金额必须大于0");
        }
        FinReceivable r = receivableMapper.selectById(receivableId);
        if (r == null) {
            throw new BusinessException("应收台账不存在: " + receivableId);
        }
        BigDecimal currentReceived = nvl(r.getReceivedAmount());
        if (currentReceived.add(amount).compareTo(r.getReceivableAmount()) > 0) {
            throw new BusinessException("核销金额超过应收金额");
        }
        BigDecimal newReceived = currentReceived.add(amount);
        r.setReceivedAmount(newReceived);
        r.setStatus(deriveStatus(newReceived, r.getReceivableAmount()));
        receivableMapper.updateById(r);
        log.info("应收核销: receivableId={}, amount={}, 已收={}/{}", receivableId, amount, newReceived, r.getReceivableAmount());
        return r;
    }

    /**
     * 按 deliveryId 查找。
     */
    public FinReceivable findByDeliveryId(Long deliveryId) {
        return receivableMapper.selectOne(
                new LambdaQueryWrapper<FinReceivable>().eq(FinReceivable::getDeliveryId, deliveryId));
    }

    // ---- private ----

    private String deriveStatus(BigDecimal received, BigDecimal total) {
        if (received.compareTo(total) >= 0) return DocumentStatus.FULLY_PAID.getCode();
        if (received.compareTo(BigDecimal.ZERO) > 0) return DocumentStatus.PARTIALLY_PAID.getCode();
        return DocumentStatus.PENDING.getCode();
    }

    private static BigDecimal nvl(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}
