package top.huanyu666.backend.common.config;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.huanyu666.backend.modules.system.entity.SysPermission;
import top.huanyu666.backend.modules.system.entity.SysRole;
import top.huanyu666.backend.modules.system.entity.SysRolePermission;
import top.huanyu666.backend.modules.system.entity.SysUserRole;
import top.huanyu666.backend.modules.system.mapper.SysPermissionMapper;
import top.huanyu666.backend.modules.system.mapper.SysRoleMapper;
import top.huanyu666.backend.modules.system.mapper.SysRolePermissionMapper;
import top.huanyu666.backend.modules.system.mapper.SysUserRoleMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sa-Token 权限加载实现：从数据库查询用户的角色和权限码
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());

        // 查用户的所有角色ID
        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        ).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查这些角色关联的所有权限ID
        List<Long> permIds = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().in(SysRolePermission::getRoleId, roleIds)
        ).stream().map(SysRolePermission::getPermissionId).distinct().collect(Collectors.toList());

        if (permIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查权限码（只取有 code 的，排除菜单）
        return permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .in(SysPermission::getId, permIds)
                        .eq(SysPermission::getStatus, 1)
                        .isNotNull(SysPermission::getCode)
                        .ne(SysPermission::getCode, "")
        ).stream().map(SysPermission::getCode).distinct().collect(Collectors.toList());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());

        // 查用户的所有角色ID
        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        ).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查角色编码
        return roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getId, roleIds)
                        .eq(SysRole::getStatus, 1)
        ).stream().map(SysRole::getCode).collect(Collectors.toList());
    }
}
