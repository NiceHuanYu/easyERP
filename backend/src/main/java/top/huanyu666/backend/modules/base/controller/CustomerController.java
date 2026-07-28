package top.huanyu666.backend.modules.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.controller.BaseBizController;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import cn.dev33.satoken.annotation.SaCheckPermission;
import top.huanyu666.backend.modules.base.entity.Customer;
import top.huanyu666.backend.modules.base.mapper.CustomerMapper;

@RestController
@RequestMapping({"/api/v1/base/customers", "/api/v1/base-data/customers"})
public class CustomerController extends BaseBizController<Customer, CustomerMapper> {

    private final CustomerMapper customerMapper;

    public CustomerController(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }
    @Override protected CustomerMapper getMapper() { return customerMapper; }

    @SaCheckPermission("base-data:customer:view")
    @GetMapping
    public ApiResponse<PageResult<Customer>> list(PageParam param,
                                                   @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Customer> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(Customer::getName, keyword).or().like(Customer::getCode, keyword));
        }
        w.orderByDesc(Customer::getCreateTime);
        Page<Customer> page = customerMapper.selectPage(new Page<>(param.getPage(), param.getSize()), w);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("base-data:customer:view")
    @GetMapping("/{id}")
    public ApiResponse<Customer> getById(@PathVariable Long id) { return doGetById(id); }

    @SaCheckPermission("base-data:customer:create")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody Customer e) { return doCreate(e); }

    @SaCheckPermission("base-data:customer:edit")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Customer e) { return doUpdate(id, e); }

    @SaCheckPermission("base-data:customer:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) { return doDelete(id); }
}
