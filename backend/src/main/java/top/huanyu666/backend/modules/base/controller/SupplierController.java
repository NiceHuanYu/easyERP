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
import top.huanyu666.backend.modules.base.entity.Supplier;
import top.huanyu666.backend.modules.base.mapper.SupplierMapper;

@RestController
@RequestMapping({"/api/v1/base/suppliers", "/api/v1/base-data/suppliers"})
public class SupplierController extends BaseBizController<Supplier, SupplierMapper> {

    private final SupplierMapper supplierMapper;

    public SupplierController(SupplierMapper supplierMapper) {
        this.supplierMapper = supplierMapper;
    }
    @Override protected SupplierMapper getMapper() { return supplierMapper; }

    @SaCheckPermission("base-data:supplier:view")
    @GetMapping
    public ApiResponse<PageResult<Supplier>> list(PageParam param,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) String code,
                                                   @RequestParam(required = false) String name,
                                                   @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Supplier> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(Supplier::getName, keyword).or().like(Supplier::getCode, keyword));
        }
        if (StringUtils.hasText(code)) w.like(Supplier::getCode, code);
        if (StringUtils.hasText(name)) w.like(Supplier::getName, name);
        if (status != null) w.eq(Supplier::getStatus, status);
        w.orderByDesc(Supplier::getCreateTime);
        Page<Supplier> page = supplierMapper.selectPage(new Page<>(param.getPage(), param.getSize()), w);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("base-data:supplier:view") @GetMapping("/{id}")
    public ApiResponse<Supplier> getById(@PathVariable Long id) { return doGetById(id); }
    @SaCheckPermission("base-data:supplier:create") @PostMapping
    public ApiResponse<Void> create(@RequestBody Supplier e) { return doCreate(e); }
    @SaCheckPermission("base-data:supplier:edit") @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Supplier e) { return doUpdate(id, e); }
    @SaCheckPermission("base-data:supplier:delete") @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) { return doDelete(id); }
}
