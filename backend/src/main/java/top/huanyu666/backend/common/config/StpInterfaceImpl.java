package top.huanyu666.backend.common.config;

import cn.dev33.satoken.stp.StpInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.huanyu666.backend.modules.system.mapper.SysPermissionMapper;
import top.huanyu666.backend.modules.system.mapper.SysRoleMapper;

import java.util.List;

/**
 * Sa-Token 权限加载实现：一次 JOIN 查询权限码与角色编码。
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysPermissionMapper permissionMapper;
    private final SysRoleMapper roleMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());
        return permissionMapper.selectPermissionCodesByUserId(userId);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());
        return roleMapper.selectRoleCodesByUserId(userId);
    }
}
