package top.huanyu666.backend.modules.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import top.huanyu666.backend.modules.base.entity.CounterpartyAccount;
import top.huanyu666.backend.modules.base.entity.Customer;
import top.huanyu666.backend.modules.base.entity.Supplier;
import top.huanyu666.backend.modules.base.mapper.CounterpartyAccountMapper;
import top.huanyu666.backend.modules.base.mapper.CustomerMapper;
import top.huanyu666.backend.modules.base.mapper.SupplierMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/base/counterparty-accounts")
@RequiredArgsConstructor
public class CounterpartyAccountController {

    private final CounterpartyAccountMapper mapper;
    private final CustomerMapper customerMapper;
    private final SupplierMapper supplierMapper;

    @GetMapping
    @SaCheckPermission("finance:bank-account:view")
    public ApiResponse<PageResult<CounterpartyAccount>> list(PageParam param,
            @RequestParam(required = false) String ownerType,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<CounterpartyAccount> qw = new LambdaQueryWrapper<>();
        if (ownerType != null && !ownerType.isBlank()) qw.eq(CounterpartyAccount::getOwnerType, ownerType);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(CounterpartyAccount::getBankName, keyword)
                    .or().like(CounterpartyAccount::getAccountNo, keyword)
                    .or().like(CounterpartyAccount::getAccountName, keyword));
        }
        qw.orderByDesc(CounterpartyAccount::getCreateTime);
        Page<CounterpartyAccount> page = mapper.selectPage(new Page<>(param.getPage(), param.getSize()), qw);
        // 填充 ownerName
        for (CounterpartyAccount a : page.getRecords()) {
            if ("CUSTOMER".equals(a.getOwnerType()) && a.getOwnerId() != null) {
                Customer c = customerMapper.selectById(a.getOwnerId());
                a.setOwnerName(c != null ? c.getName() : "");
            } else if ("SUPPLIER".equals(a.getOwnerType()) && a.getOwnerId() != null) {
                Supplier s = supplierMapper.selectById(a.getOwnerId());
                a.setOwnerName(s != null ? s.getName() : "");
            }
        }
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @GetMapping("/by-owner")
    @SaCheckPermission("finance:bank-account:view")
    public ApiResponse<List<CounterpartyAccount>> byOwner(
            @RequestParam String ownerType, @RequestParam Long ownerId) {
        List<CounterpartyAccount> list = mapper.selectList(
                new LambdaQueryWrapper<CounterpartyAccount>()
                        .eq(CounterpartyAccount::getOwnerType, ownerType)
                        .eq(CounterpartyAccount::getOwnerId, ownerId)
                        .eq(CounterpartyAccount::getStatus, 1)
                        .orderByDesc(CounterpartyAccount::getCreateTime));
        return ApiResponse.ok(list);
    }

    @PostMapping
    @SaCheckPermission("finance:bank-account:create")
    public ApiResponse<CounterpartyAccount> create(@RequestBody CounterpartyAccount account) {
        mapper.insert(account);
        return ApiResponse.ok(account);
    }

    @PutMapping("/{id}")
    @SaCheckPermission("finance:bank-account:edit")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody CounterpartyAccount account) {
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
}
