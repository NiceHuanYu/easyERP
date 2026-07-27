package top.huanyu666.backend.modules.finance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import top.huanyu666.backend.modules.finance.entity.FinPayable;
import top.huanyu666.backend.modules.finance.entity.FinPayment;
import top.huanyu666.backend.modules.finance.entity.FinPaymentItem;
import top.huanyu666.backend.modules.finance.entity.FinReceivable;
import top.huanyu666.backend.modules.finance.mapper.FinPayableMapper;
import top.huanyu666.backend.modules.finance.mapper.FinPaymentItemMapper;
import top.huanyu666.backend.modules.finance.mapper.FinPaymentMapper;
import top.huanyu666.backend.modules.finance.mapper.FinReceivableMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 收付款单管理
 */
@RestController
@RequestMapping("/api/v1/finance/payments")
@RequiredArgsConstructor
@Slf4j
public class FinPaymentController {

    private final FinPaymentMapper paymentMapper;
    private final FinPaymentItemMapper paymentItemMapper;
    private final FinReceivableMapper receivableMapper;
    private final FinPayableMapper payableMapper;

    @SaCheckPermission("finance:payment:list")
    @GetMapping
    public ApiResponse<PageResult<FinPayment>> list(PageParam param) {
        LambdaQueryWrapper<FinPayment> qw = new LambdaQueryWrapper<>();
        qw.orderByDesc(FinPayment::getCreateTime);
        Page<FinPayment> page = paymentMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("finance:payment:list")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody FinPayment payment) {
        paymentMapper.insert(payment);
        return ApiResponse.ok();
    }

    @SaCheckPermission("finance:payment:list")
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        FinPayment payment = paymentMapper.selectById(id);
        if (payment == null) {
            return ApiResponse.error("收付款单不存在");
        }
        List<FinPaymentItem> items = paymentItemMapper.selectList(
                new LambdaQueryWrapper<FinPaymentItem>().eq(FinPaymentItem::getPaymentId, id));
        Map<String, Object> result = new HashMap<>();
        result.put("payment", payment);
        result.put("paymentItems", items);
        return ApiResponse.ok(result);
    }

    @SaCheckPermission("finance:payment:confirm")
    @PostMapping("/{id}/confirm")
    @Transactional
    public ApiResponse<Void> confirm(@PathVariable Long id) {
        FinPayment payment = paymentMapper.selectById(id);
        if (payment == null) {
            return ApiResponse.error("收付款单不存在");
        }
        if (!"DRAFT".equals(payment.getStatus())) {
            return ApiResponse.error("仅草稿状态可确认");
        }

        List<FinPaymentItem> items = paymentItemMapper.selectList(
                new LambdaQueryWrapper<FinPaymentItem>().eq(FinPaymentItem::getPaymentId, id));

        for (FinPaymentItem item : items) {
            if (item.getReceivableId() != null) {
                FinReceivable receivable = receivableMapper.selectById(item.getReceivableId());
                if (receivable == null) {
                    return ApiResponse.error("应收台账不存在: " + item.getReceivableId());
                }
                BigDecimal newReceived = receivable.getReceivedAmount().add(item.getAmount());
                receivable.setReceivedAmount(newReceived);
                if (newReceived.compareTo(receivable.getReceivableAmount()) >= 0) {
                    receivable.setStatus("FULLY_PAID");
                } else if (newReceived.compareTo(BigDecimal.ZERO) > 0) {
                    receivable.setStatus("PARTIALLY_PAID");
                }
                receivableMapper.updateById(receivable);
            } else if (item.getPayableId() != null) {
                FinPayable payable = payableMapper.selectById(item.getPayableId());
                if (payable == null) {
                    return ApiResponse.error("应付台账不存在: " + item.getPayableId());
                }
                BigDecimal newPaid = payable.getPaidAmount().add(item.getAmount());
                payable.setPaidAmount(newPaid);
                if (newPaid.compareTo(payable.getPayableAmount()) >= 0) {
                    payable.setStatus("FULLY_PAID");
                } else if (newPaid.compareTo(BigDecimal.ZERO) > 0) {
                    payable.setStatus("PARTIALLY_PAID");
                }
                payableMapper.updateById(payable);
            }
        }

        payment.setStatus("CONFIRMED");
        paymentMapper.updateById(payment);
        return ApiResponse.ok();
    }
}
