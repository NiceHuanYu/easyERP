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
import top.huanyu666.backend.modules.base.entity.Warehouse;
import top.huanyu666.backend.modules.base.mapper.WarehouseMapper;

@RestController
@RequestMapping({"/api/v1/base/warehouses", "/api/v1/base-data/warehouses"})
public class WarehouseController extends BaseBizController<Warehouse, WarehouseMapper> {

    private final WarehouseMapper warehouseMapper;

    public WarehouseController(WarehouseMapper warehouseMapper) {
        this.warehouseMapper = warehouseMapper;
    }
    @Override protected WarehouseMapper getMapper() { return warehouseMapper; }

    @SaCheckPermission("base-data:warehouse:view")
    @GetMapping
    public ApiResponse<PageResult<Warehouse>> list(PageParam param,
                                                    @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Warehouse> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(Warehouse::getName, keyword).or().like(Warehouse::getCode, keyword));
        }
        w.orderByDesc(Warehouse::getCreateTime);
        Page<Warehouse> page = warehouseMapper.selectPage(new Page<>(param.getPage(), param.getSize()), w);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("base-data:warehouse:view") @GetMapping("/{id}")
    public ApiResponse<Warehouse> getById(@PathVariable Long id) { return doGetById(id); }
    @SaCheckPermission("base-data:warehouse:create") @PostMapping
    public ApiResponse<Void> create(@RequestBody Warehouse e) { return doCreate(e); }
    @SaCheckPermission("base-data:warehouse:edit") @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Warehouse e) { return doUpdate(id, e); }
    @SaCheckPermission("base-data:warehouse:delete") @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) { return doDelete(id); }
}
