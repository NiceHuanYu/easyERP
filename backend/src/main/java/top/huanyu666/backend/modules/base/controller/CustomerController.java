package top.huanyu666.backend.modules.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import cn.dev33.satoken.annotation.SaCheckPermission;
import top.huanyu666.backend.modules.base.entity.Customer;
import top.huanyu666.backend.modules.base.mapper.CustomerMapper;

/**
 * 客户管理
 */
@RestController
@RequestMapping({"/api/v1/base/customers", "/api/v1/base-data/customers"})
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerMapper customerMapper;

    @SaCheckPermission("base-data:customer:view")
    @GetMapping
    public ApiResponse<PageResult<Customer>> list(PageParam param,
                                                   @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Customer::getName, keyword)
                    .or().like(Customer::getCode, keyword));
        }
        wrapper.orderByDesc(Customer::getCreateTime);
        Page<Customer> page = customerMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), wrapper);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(),
                page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("base-data:customer:view")
    @GetMapping("/{id}")
    public ApiResponse<Customer> getById(@PathVariable Long id) {
        return ApiResponse.ok(customerMapper.selectById(id));
    }

    @SaCheckPermission("base-data:customer:create")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody Customer customer) {
        customerMapper.insert(customer);
        return ApiResponse.ok();
    }

    @SaCheckPermission("base-data:customer:edit")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Customer customer) {
        customer.setId(id);
        customerMapper.updateById(customer);
        return ApiResponse.ok();
    }

    @SaCheckPermission("base-data:customer:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        customerMapper.deleteById(id);
        return ApiResponse.ok();
    }
}
