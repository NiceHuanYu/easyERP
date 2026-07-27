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
import top.huanyu666.backend.modules.finance.mapper.FinReceivableMapper;

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

    @SaCheckPermission("finance:order:view")
    @GetMapping
    public ApiResponse<PageResult<FinReceivable>> list(PageParam param,
                                                        @RequestParam(required = false) Long customerId,
                                                        @RequestParam(required = false) String status) {
        LambdaQueryWrapper<FinReceivable> qw = new LambdaQueryWrapper<>();
        if (customerId != null) {
            qw.eq(FinReceivable::getCustomerId, customerId);
        }
        if (status != null) {
            qw.eq(FinReceivable::getStatus, status);
        }
        qw.orderByDesc(FinReceivable::getCreateTime);
        Page<FinReceivable> page = receivableMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    // ==================== 核销 ====================

    @SaCheckPermission("finance:order:view")
    @PostMapping("/{no}/reconcile")
    @Transactional
    public ApiResponse<Void> reconcile(@PathVariable String no, @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        FinReceivable receivable = receivableMapper.selectOne(
            new LambdaQueryWrapper<FinReceivable>().eq(FinReceivable::getDeliveryId,
                java.lang.Long.parseLong(no))
        );
        if (receivable == null) {
            receivable = receivableMapper.selectById(java.lang.Long.parseLong(no));
        }
        if (receivable == null) throw new BusinessException("应收记录不存在");
        receivable.setReceivedAmount(receivable.getReceivedAmount().add(amount));
        if (receivable.getReceivedAmount().compareTo(receivable.getReceivableAmount()) >= 0) {
            receivable.setStatus("PAID");
        } else {
            receivable.setStatus("PARTIAL_PAID");
        }
        receivableMapper.updateById(receivable);
        return ApiResponse.ok();
    }
}
