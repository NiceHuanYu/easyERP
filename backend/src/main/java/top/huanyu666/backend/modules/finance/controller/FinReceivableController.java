package top.huanyu666.backend.modules.finance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import top.huanyu666.backend.modules.finance.entity.FinReceivable;
import top.huanyu666.backend.modules.finance.mapper.FinReceivableMapper;

/**
 * 应收台账管理
 */
@RestController
@RequestMapping("/api/v1/finance/receivables")
@RequiredArgsConstructor
@Slf4j
public class FinReceivableController {

    private final FinReceivableMapper receivableMapper;

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
}
