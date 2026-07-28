package top.huanyu666.backend.modules.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.huanyu666.backend.common.enums.DocumentStatus;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.modules.finance.entity.FinPayable;
import top.huanyu666.backend.modules.finance.mapper.FinPayableMapper;

import java.math.BigDecimal;

/**
 * 应付台账服务。
 * <p>
 * 调用方负责 {@code @Transactional} 事务边界。
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FinPayableService {

    private final FinPayableMapper payableMapper;

    /**
     * 收货确认时创建应付台账。
     */
    public FinPayable createFromReceiving(Long receivingId, Long supplierId, BigDecimal amount) {
        FinPayable payable = new FinPayable();
        payable.setReceivingId(receivingId);
        payable.setSupplierId(supplierId);
        payable.setPayableAmount(amount);
        payable.setPaidAmount(BigDecimal.ZERO);
        payable.setStatus(DocumentStatus.UNPAID.getCode());
        payableMapper.insert(payable);
        log.info("创建应付台账: receivingId={}, supplierId={}, amount={}", receivingId, supplierId, amount);
        return payable;
    }

    /**
     * 付款核销 —— 增加已付金额，自动更新状态。
     */
    public FinPayable applyPayment(Long payableId, BigDecimal amount) {
        FinPayable p = payableMapper.selectById(payableId);
        if (p == null) {
            throw new BusinessException("应付台账不存在: " + payableId);
        }
        BigDecimal newPaid = nvl(p.getPaidAmount()).add(amount);
        p.setPaidAmount(newPaid);
        p.setStatus(deriveStatus(newPaid, p.getPayableAmount()));
        payableMapper.updateById(p);
        log.info("应付核销: payableId={}, amount={}, 已付={}/{}", payableId, amount, newPaid, p.getPayableAmount());
        return p;
    }

    /**
     * 按 receivingId 查找。
     */
    public FinPayable findByReceivingId(Long receivingId) {
        return payableMapper.selectOne(
                new LambdaQueryWrapper<FinPayable>().eq(FinPayable::getReceivingId, receivingId));
    }

    // ---- private ----

    private String deriveStatus(BigDecimal paid, BigDecimal total) {
        if (paid.compareTo(total) >= 0) return DocumentStatus.FULLY_PAID.getCode();
        if (paid.compareTo(BigDecimal.ZERO) > 0) return DocumentStatus.PARTIALLY_PAID.getCode();
        return DocumentStatus.UNPAID.getCode();
    }

    private static BigDecimal nvl(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}
