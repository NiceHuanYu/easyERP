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
import top.huanyu666.backend.modules.finance.mapper.FinPayableMapper;

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

    @SaCheckPermission("finance:payable:list")
    @GetMapping
    public ApiResponse<PageResult<FinPayable>> list(PageParam param,
                                                     @RequestParam(required = false) Long supplierId,
                                                     @RequestParam(required = false) String status) {
        LambdaQueryWrapper<FinPayable> qw = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            qw.eq(FinPayable::getSupplierId, supplierId);
        }
        if (status != null) {
            qw.eq(FinPayable::getStatus, status);
        }
        qw.orderByDesc(FinPayable::getCreateTime);
        Page<FinPayable> page = payableMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    // ==================== 核销 ====================

    @SaCheckPermission("finance:payable:list")
    @PostMapping("/{no}/reconcile")
    @Transactional
    public ApiResponse<Void> reconcile(@PathVariable String no, @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        FinPayable payable = payableMapper.selectById(java.lang.Long.parseLong(no));
        if (payable == null) throw new BusinessException("应付记录不存在");
        payable.setPaidAmount(payable.getPaidAmount().add(amount));
        if (payable.getPaidAmount().compareTo(payable.getPayableAmount()) >= 0) {
            payable.setStatus("PAID");
        } else {
            payable.setStatus("PARTIAL_PAID");
        }
        payableMapper.updateById(payable);
        return ApiResponse.ok();
    }
}
