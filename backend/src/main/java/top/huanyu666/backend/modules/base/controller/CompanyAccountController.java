package top.huanyu666.backend.modules.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import top.huanyu666.backend.modules.base.entity.CompanyAccount;
import top.huanyu666.backend.modules.base.mapper.CompanyAccountMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/base/company-accounts")
@RequiredArgsConstructor
public class CompanyAccountController {

    private final CompanyAccountMapper mapper;

    @GetMapping
    @SaCheckPermission("finance:bank-account:view")
    public ApiResponse<PageResult<CompanyAccount>> list(PageParam param,
                                                         @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<CompanyAccount> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(CompanyAccount::getBankName, keyword)
                    .or().like(CompanyAccount::getAccountNo, keyword)
                    .or().like(CompanyAccount::getAccountName, keyword));
        }
        qw.orderByDesc(CompanyAccount::getCreateTime);
        Page<CompanyAccount> page = mapper.selectPage(new Page<>(param.getPage(), param.getSize()), qw);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("finance:bank-account:view")
    public ApiResponse<CompanyAccount> getById(@PathVariable Long id) {
        return ApiResponse.ok(mapper.selectById(id));
    }

    @PostMapping
    @SaCheckPermission("finance:bank-account:create")
    public ApiResponse<CompanyAccount> create(@RequestBody CompanyAccount account) {
        mapper.insert(account);
        return ApiResponse.ok(account);
    }

    @PutMapping("/{id}")
    @SaCheckPermission("finance:bank-account:edit")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody CompanyAccount account) {
        account.setId(id);
        mapper.updateById(account);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("finance:bank-account:delete")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        mapper.deleteById(id);
        return ApiResponse.ok();
    }

    /** 全部（下拉框用） */
    @GetMapping("/all")
    @SaCheckPermission("finance:bank-account:view")
    public ApiResponse<List<CompanyAccount>> all(
            @RequestParam(required = false) String accountType) {
        LambdaQueryWrapper<CompanyAccount> qw = new LambdaQueryWrapper<>();
        if (accountType != null && !accountType.isBlank()) {
            // BOTH 始终包含；RECEIVE 匹配 RECEIVE 和 BOTH；PAY 匹配 PAY 和 BOTH
            if ("RECEIVE".equals(accountType)) {
                qw.in(CompanyAccount::getAccountType, "RECEIVE", "BOTH");
            } else {
                qw.in(CompanyAccount::getAccountType, "PAY", "BOTH");
            }
        }
        qw.eq(CompanyAccount::getStatus, 1);
        qw.orderByDesc(CompanyAccount::getCreateTime);
        return ApiResponse.ok(mapper.selectList(qw));
    }
}
