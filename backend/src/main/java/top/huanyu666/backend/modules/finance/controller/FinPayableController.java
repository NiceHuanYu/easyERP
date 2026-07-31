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
import top.huanyu666.backend.modules.finance.entity.FinPayable;
import top.huanyu666.backend.modules.finance.entity.FinPayment;
import top.huanyu666.backend.modules.finance.mapper.FinPayableMapper;
import top.huanyu666.backend.modules.finance.mapper.FinPaymentMapper;
import top.huanyu666.backend.modules.finance.service.FinPayableService;
import top.huanyu666.backend.modules.base.entity.Supplier;
import top.huanyu666.backend.modules.base.mapper.SupplierMapper;
import top.huanyu666.backend.modules.purchase.entity.PurReceiving;
import top.huanyu666.backend.modules.purchase.mapper.PurReceivingMapper;

import java.math.BigDecimal;
import java.util.Map;
import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 应付台账管理
 */
@RestController
@RequestMapping("/api/v1/finance/payables")
@RequiredArgsConstructor
@Slf4j
public class FinPayableController {

    private final FinPayableMapper payableMapper;
    private final FinPayableService payableService;
    private final FinPaymentMapper paymentMapper;
    private final SupplierMapper supplierMapper;
    private final PurReceivingMapper receivingMapper;

    @SaCheckPermission("finance:order:view")
    @GetMapping
    public ApiResponse<PageResult<FinPayable>> list(PageParam param,
                                                     @RequestParam(required = false) Long supplierId,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(required = false) String startDate,
                                                     @RequestParam(required = false) String endDate) {
        LambdaQueryWrapper<FinPayable> qw = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            qw.eq(FinPayable::getSupplierId, supplierId);
        }
        if (status != null) {
            qw.eq(FinPayable::getStatus, status);
        }
        if (startDate != null && !startDate.isBlank()) {
            qw.ge(FinPayable::getDueDate, java.time.LocalDate.parse(startDate));
        }
        if (endDate != null && !endDate.isBlank()) {
            qw.le(FinPayable::getDueDate, java.time.LocalDate.parse(endDate));
        }
        qw.orderByDesc(FinPayable::getCreateTime);
        Page<FinPayable> page = payableMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        page.getRecords().forEach(p -> {
            // 供应商名
            if (p.getSupplierId() != null) {
                Supplier s = supplierMapper.selectById(p.getSupplierId());
                p.setSupplierName(s != null ? s.getName() : "");
            }
            // 收货单号 + 应付单号
            if (p.getReceivingId() != null) {
                PurReceiving r = receivingMapper.selectById(p.getReceivingId());
                p.setReceivingNo(r != null ? r.getReceivingNo() : "");
            }
            p.setPayableNo("AP-" + p.getId());
        });
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    // ==================== 核销 ====================

    /** 更新应付日期 */
    @SaCheckPermission("finance:order:edit")
    @PutMapping("/{id}/due-date")
    public ApiResponse<Void> updateDueDate(@PathVariable Long id, @RequestBody Map<String, String> body) {
        FinPayable p = payableMapper.selectById(id);
        if (p == null) throw new BusinessException("应付记录不存在");
        p.setDueDate(java.time.LocalDate.parse(body.get("dueDate")));
        payableMapper.updateById(p);
        return ApiResponse.ok();
    }

    @SaCheckPermission("finance:order:approve")
    @PostMapping("/{id}/reconcile")
    @Transactional
    public ApiResponse<Void> reconcile(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        payableService.applyPayment(id, amount);

        // 更新关联的收付款单已核销金额
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> payments =
                (java.util.List<java.util.Map<String, Object>>) body.get("payments");
        if (payments != null) {
            for (java.util.Map<String, Object> p : payments) {
                String paymentNo = (String) p.get("paymentNo");
                BigDecimal payAmount = new BigDecimal(p.get("amount").toString());
                FinPayment payment = paymentMapper.selectOne(
                        new LambdaQueryWrapper<FinPayment>().eq(FinPayment::getPaymentNo, paymentNo));
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
