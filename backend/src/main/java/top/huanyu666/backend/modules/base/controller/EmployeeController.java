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
import top.huanyu666.backend.modules.base.entity.Employee;
import top.huanyu666.backend.modules.base.mapper.EmployeeMapper;

@RestController
@RequestMapping({"/api/v1/base/employees", "/api/v1/base-data/employees"})
public class EmployeeController extends BaseBizController<Employee, EmployeeMapper> {

    private final EmployeeMapper employeeMapper;

    public EmployeeController(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }
    @Override protected EmployeeMapper getMapper() { return employeeMapper; }

    @SaCheckPermission("base-data:employee:view")
    @GetMapping
    public ApiResponse<PageResult<Employee>> list(PageParam param,
                                                   @RequestParam(required = false) String code,
                                                   @RequestParam(required = false) String name,
                                                   @RequestParam(required = false) String dept,
                                                   @RequestParam(required = false) Integer status,
                                                   @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Employee> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(code)) w.eq(Employee::getCode, code);
        if (StringUtils.hasText(name)) w.like(Employee::getName, name);
        if (StringUtils.hasText(dept)) w.eq(Employee::getDept, dept);
        if (status != null) w.eq(Employee::getStatus, status);
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(Employee::getName, keyword).or().like(Employee::getCode, keyword));
        }
        w.orderByDesc(Employee::getCreateTime);
        Page<Employee> page = employeeMapper.selectPage(new Page<>(param.getPage(), param.getSize()), w);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("base-data:employee:view") @GetMapping("/{id}")
    public ApiResponse<Employee> getById(@PathVariable Long id) { return doGetById(id); }
    @SaCheckPermission("base-data:employee:create") @PostMapping
    public ApiResponse<Void> create(@RequestBody Employee e) { return doCreate(e); }
    @SaCheckPermission("base-data:employee:edit") @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Employee e) { return doUpdate(id, e); }
    @SaCheckPermission("base-data:employee:delete") @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) { return doDelete(id); }
}
