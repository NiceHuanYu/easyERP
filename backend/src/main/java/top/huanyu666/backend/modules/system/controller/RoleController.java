package top.huanyu666.backend.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.modules.system.entity.SysRole;
import top.huanyu666.backend.modules.system.entity.SysRolePermission;
import top.huanyu666.backend.modules.system.entity.SysPermission;
import top.huanyu666.backend.modules.system.entity.SysUserRole;
import top.huanyu666.backend.modules.system.mapper.SysRoleMapper;
import top.huanyu666.backend.modules.system.mapper.SysRolePermissionMapper;
import top.huanyu666.backend.modules.system.mapper.SysPermissionMapper;
import top.huanyu666.backend.modules.system.mapper.SysUserRoleMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色管理
 */
@RestController
@RequestMapping("/api/v1/system/roles")
@RequiredArgsConstructor
public class RoleController {

    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysUserRoleMapper userRoleMapper;

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
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
        roleMapper.deleteById(id);
        return ApiResponse.ok();
    }

    /** 获取角色已有权限ID列表 */
    @GetMapping("/{id}/permissions")
    @SaCheckPermission("system:role:view")
    public ApiResponse<List<Long>> getRolePermissions(@PathVariable Long id) {
        List<SysRolePermission> list = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));
        return ApiResponse.ok(list.stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList()));
    }

    /** 设置角色权限 */
    @PutMapping("/{id}/permissions")
    @SaCheckPermission("system:role:update")
    public ApiResponse<Void> setRolePermissions(@PathVariable Long id, @RequestBody List<Long> permIds) {
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));
        for (Long permId : permIds) {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(id);
            rp.setPermissionId(permId);
            rolePermissionMapper.insert(rp);
        }
        return ApiResponse.ok();
    }

    /** 获取所有权限（树形结构，供分配权限用） */
    @GetMapping("/permissions")
    @SaCheckPermission("system:role:view")
    public ApiResponse<List<Map<String, Object>>> allPermissions() {
        List<SysPermission> all = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getStatus, 1)
                        .orderByAsc(SysPermission::getSort));
        return ApiResponse.ok(buildPermTree(all, 0L));
    }

    private List<Map<String, Object>> buildPermTree(List<SysPermission> all, Long parentId) {
        return all.stream().filter(p -> parentId.equals(p.getParentId())).map(p -> {
            Map<String, Object> node = new java.util.HashMap<>();
            node.put("id", p.getId());
            node.put("label", p.getName());
            node.put("code", p.getCode());
            List<Map<String, Object>> children = buildPermTree(all, p.getId());
            if (!children.isEmpty()) node.put("children", children);
            return node;
        }).collect(Collectors.toList());
    }
}
