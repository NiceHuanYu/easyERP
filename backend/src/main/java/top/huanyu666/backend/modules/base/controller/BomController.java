package top.huanyu666.backend.modules.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import cn.dev33.satoken.annotation.SaCheckPermission;
import top.huanyu666.backend.modules.base.entity.Bom;
import top.huanyu666.backend.modules.base.mapper.BomMapper;

/**
 * BOM 管理
 */
@RestController
@RequestMapping({"/api/v1/base/boms", "/api/v1/base-data/boms"})
@RequiredArgsConstructor
public class BomController {

    private final BomMapper bomMapper;

    @SaCheckPermission("base:bom:list")
    @GetMapping
    public ApiResponse<PageResult<Bom>> list(PageParam param,
                                              @RequestParam(required = false) Long parentMaterialId) {
        LambdaQueryWrapper<Bom> wrapper = new LambdaQueryWrapper<>();
        if (parentMaterialId != null) {
            wrapper.eq(Bom::getParentMaterialId, parentMaterialId);
        }
        wrapper.orderByAsc(Bom::getSort);
        Page<Bom> page = bomMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), wrapper);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(),
                page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("base:bom:list")
    @GetMapping("/{id}")
    public ApiResponse<Bom> getById(@PathVariable Long id) {
        return ApiResponse.ok(bomMapper.selectById(id));
    }

    @SaCheckPermission("base:material:create")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody Bom bom) {
        bomMapper.insert(bom);
        return ApiResponse.ok();
    }

    @SaCheckPermission("base:material:update")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Bom bom) {
        bom.setId(id);
        bomMapper.updateById(bom);
        return ApiResponse.ok();
    }

    @SaCheckPermission("base:material:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bomMapper.deleteById(id);
        return ApiResponse.ok();
    }
}
