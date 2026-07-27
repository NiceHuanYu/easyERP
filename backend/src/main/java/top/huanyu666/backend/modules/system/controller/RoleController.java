package top.huanyu666.backend.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.modules.system.entity.SysRole;
import top.huanyu666.backend.modules.system.mapper.SysRoleMapper;

import java.util.List;

/**
 * 角色管理
 */
@RestController
@RequestMapping("/api/v1/system/roles")
@RequiredArgsConstructor
public class RoleController {

    private final SysRoleMapper roleMapper;

    @GetMapping
    @SaCheckPermission("system:role:view")
    public ApiResponse<List<SysRole>> list() {
        return ApiResponse.ok(roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().orderByDesc(SysRole::getCreateTime)
        ));
    }

    @PostMapping
    @SaCheckPermission("system:role:create")
    public ApiResponse<Void> create(@RequestBody SysRole role) {
        roleMapper.insert(role);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:role:edit")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody SysRole role) {
        role.setId(id);
        roleMapper.updateById(role);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:role:delete")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleMapper.deleteById(id);
        return ApiResponse.ok();
    }
}
