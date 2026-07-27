package top.huanyu666.backend.modules.finance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import top.huanyu666.backend.modules.finance.entity.FinPayable;
import top.huanyu666.backend.modules.finance.mapper.FinPayableMapper;
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
}
