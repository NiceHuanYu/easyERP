package top.huanyu666.backend.modules.finance.controller;

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
import top.huanyu666.backend.modules.finance.entity.FinReceivable;
import top.huanyu666.backend.modules.finance.entity.FinPayment;
import top.huanyu666.backend.modules.finance.mapper.FinReceivableMapper;
import top.huanyu666.backend.modules.finance.mapper.FinPaymentMapper;
import top.huanyu666.backend.modules.finance.service.FinReceivableService;
import top.huanyu666.backend.modules.base.entity.Customer;
import top.huanyu666.backend.modules.base.mapper.CustomerMapper;
import top.huanyu666.backend.modules.sales.entity.SalesDelivery;
import top.huanyu666.backend.modules.sales.mapper.SalesDeliveryMapper;

import java.math.BigDecimal;
import java.util.Map;
import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 应收台账管理
 */
@RestController
@RequestMapping("/api/v1/finance/receivables")
@RequiredArgsConstructor
@Slf4j
public class FinReceivableController {

    private final FinReceivableMapper receivableMapper;
    private final FinReceivableService receivableService;
    private final FinPaymentMapper paymentMapper;
    private final CustomerMapper customerMapper;
    private final SalesDeliveryMapper deliveryMapper;

    @SaCheckPermission("finance:order:view")
    @GetMapping
    public ApiResponse<PageResult<FinReceivable>> list(PageParam param,
                                                        @RequestParam(required = false) Long customerId,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(required = false) String startDate,
                                                        @RequestParam(required = false) String endDate) {
        LambdaQueryWrapper<FinReceivable> qw = new LambdaQueryWrapper<>();
        if (customerId != null) {
            qw.eq(FinReceivable::getCustomerId, customerId);
        }
        if (status != null) {
            qw.eq(FinReceivable::getStatus, status);
        }
        if (startDate != null && !startDate.isBlank()) {
            qw.ge(FinReceivable::getDueDate, java.time.LocalDate.parse(startDate));
        }
        if (endDate != null && !endDate.isBlank()) {
            qw.le(FinReceivable::getDueDate, java.time.LocalDate.parse(endDate));
        }
        qw.orderByDesc(FinReceivable::getCreateTime);
        Page<FinReceivable> page = receivableMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        page.getRecords().forEach(r -> {
            if (r.getCustomerId() != null) {
                Customer c = customerMapper.selectById(r.getCustomerId());
                r.setCustomerName(c != null ? c.getName() : "");
            }
            if (r.getDeliveryId() != null) {
                SalesDelivery d = deliveryMapper.selectById(r.getDeliveryId());
                r.setDeliveryNo(d != null ? d.getDeliveryNo() : "");
            }
            r.setReceivableNo("AR-" + r.getId());
        });
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    // ==================== 核销 ====================

    /** 更新应收日期 */
    @SaCheckPermission("finance:order:edit")
    @PutMapping("/{id}/due-date")
    public ApiResponse<Void> updateDueDate(@PathVariable Long id, @RequestBody Map<String, String> body) {
        FinReceivable r = receivableMapper.selectById(id);
        if (r == null) throw new BusinessException("应收记录不存在");
        r.setDueDate(java.time.LocalDate.parse(body.get("dueDate")));
        receivableMapper.updateById(r);
        return ApiResponse.ok();
    }

    @SaCheckPermission("finance:order:approve")
    @PostMapping("/{id}/reconcile")
    @Transactional
    public ApiResponse<Void> reconcile(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        receivableService.applyPayment(id, amount);

        // 更新关联的收付款单已核销金额
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> payments =
                (java.util.List<java.util.Map<String, Object>>) body.get("payments");
        if (payments != null) {
            for (java.util.Map<String, Object> p : payments) {
                String paymentNo = (String) p.get("paymentNo");
                BigDecimal payAmount = new BigDecimal(p.get("amount").toString());
                FinPayment payment = paymentMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FinPayment>()
                                .eq(FinPayment::getPaymentNo, paymentNo));
                if (payment != null) {
                    BigDecimal current = payment.getReconciledAmount() != null
                            ? payment.getReconciledAmount() : java.math.BigDecimal.ZERO;
                    payment.setReconciledAmount(current.add(payAmount));
                    paymentMapper.updateById(payment);
                }
            }
        }
        return ApiResponse.ok();
    }
}
