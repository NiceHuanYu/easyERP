package top.huanyu666.backend.modules.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import top.huanyu666.backend.modules.base.entity.Material;
import top.huanyu666.backend.modules.base.mapper.MaterialMapper;

/**
 * 物料管理
 */
@RestController
@RequestMapping("/api/v1/base/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialMapper materialMapper;

    @GetMapping
    public ApiResponse<PageResult<Material>> list(PageParam param,
                                                   @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Material::getName, keyword)
                    .or().like(Material::getCode, keyword));
        }
        wrapper.orderByDesc(Material::getCreateTime);
        Page<Material> page = materialMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), wrapper);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(),
                page.getSize(), page.getRecords()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Material> getById(@PathVariable Long id) {
        return ApiResponse.ok(materialMapper.selectById(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Material material) {
        materialMapper.insert(material);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Material material) {
        material.setId(id);
        materialMapper.updateById(material);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        materialMapper.deleteById(id);
        return ApiResponse.ok();
    }
}
