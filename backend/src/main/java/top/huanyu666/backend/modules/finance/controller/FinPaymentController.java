package top.huanyu666.backend.modules.finance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.utils.CodeGenerator;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import top.huanyu666.backend.modules.base.entity.Customer;
import top.huanyu666.backend.modules.base.entity.Supplier;
import top.huanyu666.backend.modules.base.mapper.CustomerMapper;
import top.huanyu666.backend.modules.base.mapper.SupplierMapper;
import top.huanyu666.backend.modules.finance.entity.FinPayment;
import top.huanyu666.backend.modules.finance.entity.FinPaymentItem;
import top.huanyu666.backend.modules.finance.mapper.FinPaymentItemMapper;
import top.huanyu666.backend.modules.finance.mapper.FinPaymentMapper;
import top.huanyu666.backend.modules.finance.service.FinPayableService;
import top.huanyu666.backend.modules.finance.service.FinReceivableService;

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
    private final FinReceivableService receivableService;
    private final FinPayableService payableService;
    private final CustomerMapper customerMapper;
    private final SupplierMapper supplierMapper;

    @SaCheckPermission("finance:order:view")
    @GetMapping
    public ApiResponse<PageResult<FinPayment>> list(PageParam param,
            @RequestParam(required = false) String paymentNo,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<FinPayment> qw = new LambdaQueryWrapper<>();
        if (paymentNo != null && !paymentNo.isBlank()) qw.like(FinPayment::getPaymentNo, paymentNo);
        if (type != null && !type.isBlank()) qw.eq(FinPayment::getType, type);
        if (status != null && !status.isBlank()) qw.eq(FinPayment::getStatus, status);
        qw.orderByDesc(FinPayment::getCreateTime);
        Page<FinPayment> page = paymentMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        // 填充往来单位名称
        // 批量查客户和供应商（不依赖 type 字段，双向尝试）
        java.util.Set<Long> allIds = new java.util.HashSet<>();
        for (FinPayment p : page.getRecords()) {
            if (p.getCounterpartyId() != null) allIds.add(p.getCounterpartyId());
        }
        List<Long> idList = new java.util.ArrayList<>(allIds);
        java.util.Map<Long, String> customerMap = idList.isEmpty() ? java.util.Collections.emptyMap()
                : customerMapper.selectBatchIds(idList).stream()
                        .collect(java.util.stream.Collectors.toMap(Customer::getId, Customer::getName, (a, b) -> a));
        java.util.Map<Long, String> supplierMap = idList.isEmpty() ? java.util.Collections.emptyMap()
                : supplierMapper.selectBatchIds(idList).stream()
                        .collect(java.util.stream.Collectors.toMap(Supplier::getId, Supplier::getName, (a, b) -> a));
        for (FinPayment p : page.getRecords()) {
            if (p.getCounterpartyId() != null) {
                String name = customerMap.get(p.getCounterpartyId());
                if (name == null) name = supplierMap.get(p.getCounterpartyId());
                p.setCounterpartyName(name != null ? name : "");
            }
        }
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("finance:order:view")
    @PostMapping
    public ApiResponse<FinPayment> create(@RequestBody FinPayment payment) {
        if (payment.getPaymentNo() == null || payment.getPaymentNo().isBlank()) {
            payment.setPaymentNo(CodeGenerator.generate("PAY", () -> {
                FinPayment last = paymentMapper.selectOne(
                        new LambdaQueryWrapper<FinPayment>()
                                .select(FinPayment::getPaymentNo)
                                .orderByDesc(FinPayment::getPaymentNo)
                                .last("LIMIT 1"));
                return last != null ? last.getPaymentNo() : null;
            }));
        }
        if (payment.getStatus() == null || payment.getStatus().isBlank()) {
            payment.setStatus("DRAFT");
        }
        paymentMapper.insert(payment);
        return ApiResponse.ok(payment);
    }

    @SaCheckPermission("finance:order:view")
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        FinPayment payment = paymentMapper.selectById(id);
        if (payment == null) {
            return ApiResponse.error("收付款单不存在");
        }
        // 填充往来单位名称
        if (payment.getCounterpartyId() != null) {
            if ("RECEIVE".equals(payment.getType())) {
                Customer c = customerMapper.selectById(payment.getCounterpartyId());
                payment.setCounterpartyName(c != null ? c.getName() : "");
            } else {
                Supplier s = supplierMapper.selectById(payment.getCounterpartyId());
                payment.setCounterpartyName(s != null ? s.getName() : "");
            }
        }
        List<FinPaymentItem> items = paymentItemMapper.selectList(
                new LambdaQueryWrapper<FinPaymentItem>().eq(FinPaymentItem::getPaymentId, id));
        Map<String, Object> result = new HashMap<>();
        result.put("payment", payment);
        result.put("paymentItems", items);
        return ApiResponse.ok(result);
    }

    @SaCheckPermission("finance:order:approve")
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

        // 执行核销（若有核销明细）
        List<FinPaymentItem> items = paymentItemMapper.selectList(
                new LambdaQueryWrapper<FinPaymentItem>().eq(FinPaymentItem::getPaymentId, id));
        for (FinPaymentItem item : items) {
            if (item.getReceivableId() != null) {
                receivableService.applyPayment(item.getReceivableId(), item.getAmount());
            } else if (item.getPayableId() != null) {
                payableService.applyPayment(item.getPayableId(), item.getAmount());
            }
        }

        payment.setStatus("CONFIRMED");
        paymentMapper.updateById(payment);
        return ApiResponse.ok();
    }

    // ==================== 编辑/删除 ====================

    @SaCheckPermission("finance:order:view")
    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody FinPayment payment) {
        FinPayment exist = paymentMapper.selectById(id);
        if (exist == null) throw new BusinessException("收付款单不存在");
        if (!"DRAFT".equals(exist.getStatus())) throw new BusinessException("只有草稿状态可编辑");
        payment.setId(id);
        paymentMapper.updateById(payment);
        return ApiResponse.ok();
    }

    @SaCheckPermission("finance:order:view")
    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        FinPayment payment = paymentMapper.selectById(id);
        if (payment == null) throw new BusinessException("收付款单不存在");
        if (!"DRAFT".equals(payment.getStatus())) throw new BusinessException("只有草稿状态可删除");
        paymentItemMapper.delete(new LambdaQueryWrapper<FinPaymentItem>().eq(FinPaymentItem::getPaymentId, id));
        paymentMapper.deleteById(id);
        return ApiResponse.ok();
    }
}
