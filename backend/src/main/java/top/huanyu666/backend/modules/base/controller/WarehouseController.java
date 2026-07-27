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
import top.huanyu666.backend.modules.base.entity.Warehouse;
import top.huanyu666.backend.modules.base.mapper.WarehouseMapper;

/**
 * 仓库管理
 */
@RestController
@RequestMapping("/api/v1/base/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseMapper warehouseMapper;

    @SaCheckPermission("base:warehouse:list")
    @GetMapping
    public ApiResponse<PageResult<Warehouse>> list(PageParam param,
                                                    @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Warehouse::getName, keyword)
                    .or().like(Warehouse::getCode, keyword));
        }
        wrapper.orderByDesc(Warehouse::getCreateTime);
        Page<Warehouse> page = warehouseMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), wrapper);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(),
                page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("base:warehouse:list")
    @GetMapping("/{id}")
    public ApiResponse<Warehouse> getById(@PathVariable Long id) {
        return ApiResponse.ok(warehouseMapper.selectById(id));
    }

    @SaCheckPermission("base:warehouse:create")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody Warehouse warehouse) {
        warehouseMapper.insert(warehouse);
        return ApiResponse.ok();
    }

    @SaCheckPermission("base:warehouse:update")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Warehouse warehouse) {
        warehouse.setId(id);
        warehouseMapper.updateById(warehouse);
        return ApiResponse.ok();
    }

    @SaCheckPermission("base:warehouse:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        warehouseMapper.deleteById(id);
        return ApiResponse.ok();
    }
}
