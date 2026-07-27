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
import top.huanyu666.backend.modules.base.entity.Employee;
import top.huanyu666.backend.modules.base.mapper.EmployeeMapper;

/**
 * 员工管理
 */
@RestController
@RequestMapping({"/api/v1/base/employees", "/api/v1/base-data/employees"})
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeMapper employeeMapper;

    @SaCheckPermission("base-data:employee:view")
    @GetMapping
    public ApiResponse<PageResult<Employee>> list(PageParam param,
                                                   @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Employee::getName, keyword)
                    .or().like(Employee::getCode, keyword));
        }
        wrapper.orderByDesc(Employee::getCreateTime);
        Page<Employee> page = employeeMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), wrapper);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(),
                page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("base-data:employee:view")
    @GetMapping("/{id}")
    public ApiResponse<Employee> getById(@PathVariable Long id) {
        return ApiResponse.ok(employeeMapper.selectById(id));
    }

    @SaCheckPermission("base-data:employee:create")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody Employee employee) {
        employeeMapper.insert(employee);
        return ApiResponse.ok();
    }

    @SaCheckPermission("base-data:employee:edit")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Employee employee) {
        employee.setId(id);
        employeeMapper.updateById(employee);
        return ApiResponse.ok();
    }

    @SaCheckPermission("base-data:employee:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        employeeMapper.deleteById(id);
        return ApiResponse.ok();
    }
}
