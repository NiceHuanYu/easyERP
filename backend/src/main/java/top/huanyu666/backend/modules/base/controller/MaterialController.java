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
import top.huanyu666.backend.modules.base.entity.Material;
import top.huanyu666.backend.modules.base.mapper.MaterialMapper;

@RestController
@RequestMapping({"/api/v1/base/materials", "/api/v1/base-data/materials"})
public class MaterialController extends BaseBizController<Material, MaterialMapper> {

    private final MaterialMapper materialMapper;

    public MaterialController(MaterialMapper materialMapper) {
        this.materialMapper = materialMapper;
    }
    @Override protected MaterialMapper getMapper() { return materialMapper; }

    @SaCheckPermission("base-data:material:view")
    @GetMapping
    public ApiResponse<PageResult<Material>> list(PageParam param,
                                                   @RequestParam(required = false) String code,
                                                   @RequestParam(required = false) String name,
                                                   @RequestParam(required = false) Integer category,
                                                   @RequestParam(required = false) Integer status,
                                                   @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Material> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(code)) w.like(Material::getCode, code);
        if (StringUtils.hasText(name)) w.like(Material::getName, name);
        if (category != null) w.eq(Material::getCategory, category);
        if (status != null) w.eq(Material::getStatus, status);
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(Material::getName, keyword).or().like(Material::getCode, keyword));
        }
        w.orderByDesc(Material::getCreateTime);
        Page<Material> page = materialMapper.selectPage(new Page<>(param.getPage(), param.getSize()), w);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("base-data:material:view") @GetMapping("/{id}")
    public ApiResponse<Material> getById(@PathVariable Long id) { return doGetById(id); }
    @SaCheckPermission("base-data:material:create") @PostMapping
    public ApiResponse<Void> create(@RequestBody Material e) { return doCreate(e); }
    @SaCheckPermission("base-data:material:edit") @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Material e) { return doUpdate(id, e); }
    @SaCheckPermission("base-data:material:delete") @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) { return doDelete(id); }
}
