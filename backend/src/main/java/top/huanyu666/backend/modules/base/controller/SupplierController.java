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
import top.huanyu666.backend.modules.base.entity.Supplier;
import top.huanyu666.backend.modules.base.mapper.SupplierMapper;

/**
 * 供应商管理
 */
@RestController
@RequestMapping("/api/v1/base/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierMapper supplierMapper;

    @SaCheckPermission("base:supplier:list")
    @GetMapping
    public ApiResponse<PageResult<Supplier>> list(PageParam param,
                                                   @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Supplier::getName, keyword)
                    .or().like(Supplier::getCode, keyword));
        }
        wrapper.orderByDesc(Supplier::getCreateTime);
        Page<Supplier> page = supplierMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), wrapper);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(),
                page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("base:supplier:list")
    @GetMapping("/{id}")
    public ApiResponse<Supplier> getById(@PathVariable Long id) {
        return ApiResponse.ok(supplierMapper.selectById(id));
    }

    @SaCheckPermission("base:supplier:create")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody Supplier supplier) {
        supplierMapper.insert(supplier);
        return ApiResponse.ok();
    }

    @SaCheckPermission("base:supplier:update")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Supplier supplier) {
        supplier.setId(id);
        supplierMapper.updateById(supplier);
        return ApiResponse.ok();
    }

    @SaCheckPermission("base:supplier:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        supplierMapper.deleteById(id);
        return ApiResponse.ok();
    }
}
