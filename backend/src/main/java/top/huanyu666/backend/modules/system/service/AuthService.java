package top.huanyu666.backend.modules.system.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.modules.system.dto.LoginRequest;
import top.huanyu666.backend.modules.system.dto.MenuTree;
import top.huanyu666.backend.modules.system.dto.UserInfoResponse;
import top.huanyu666.backend.modules.system.entity.SysPermission;
import top.huanyu666.backend.modules.system.entity.SysUser;
import top.huanyu666.backend.modules.system.mapper.SysPermissionMapper;
import top.huanyu666.backend.modules.system.mapper.SysUserMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysPermissionMapper permissionMapper;

    /**
     * 登录
     */
    public String login(LoginRequest request) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername())
        );
        if (user == null || !BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            log.warn("登录失败: username={}, 原因=账号或密码错误", request.getUsername());
            throw new BusinessException("账号或密码错误");
        }
        if (user.getStatus() == 0) {
            log.warn("登录失败: username={}, userId={}, 原因=账号已禁用", request.getUsername(), user.getId());
            throw new BusinessException("账号已被禁用");
        }
        StpUtil.login(user.getId());
        log.info("登录成功: userId={}, username={}", user.getId(), user.getUsername());
        return StpUtil.getTokenValue();
    }

    /**
     * 获取当前用户信息
     */
    public UserInfoResponse getUserInfo() {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 获取权限码列表
        List<String> permissions = StpUtil.getPermissionList();

        // 获取用户拥有的权限记录 → 提取 ID 集合
        Set<Long> allowedPermIds = new HashSet<>();
        if (!permissions.isEmpty()) {
            List<SysPermission> userPerms = permissionMapper.selectList(
                    new LambdaQueryWrapper<SysPermission>()
                            .in(SysPermission::getCode, permissions)
                            .eq(SysPermission::getStatus, 1)
            );
            allowedPermIds = userPerms.stream().map(SysPermission::getId).collect(Collectors.toSet());
        }

        // 查询所有启用的菜单/按钮权限，按用户权限过滤后构建菜单树
        List<SysPermission> allPerms = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getStatus, 1)
                        .orderByAsc(SysPermission::getSort)
        );
        List<MenuTree> menus = buildMenuTreeFiltered(allPerms, 0L, allowedPermIds);

        return UserInfoResponse.builder()
                .userId(userId)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .permissions(permissions)
                .menus(menus)
                .build();
    }

    /**
     * 按用户权限过滤构建菜单树：仅保留用户有权访问的菜单/按钮节点。
     * 父节点若无权限但子节点有权限则保留（作为容器）。
     */
    private List<MenuTree> buildMenuTreeFiltered(List<SysPermission> all, Long parentId, Set<Long> allowedIds) {
        return all.stream()
                .filter(p -> parentId.equals(p.getParentId()))
                .map(p -> {
                    List<MenuTree> children = buildMenuTreeFiltered(all, p.getId(), allowedIds);
                    // 保留条件：自身在权限集中，或有可见子节点
                    if (!allowedIds.contains(p.getId()) && children.isEmpty()) {
                        return null;
                    }
                    return MenuTree.builder()
                            .id(p.getId())
                            .parentId(p.getParentId())
                            .name(p.getName())
                            .code(p.getCode())
                            .path(p.getPath())
                            .icon(p.getIcon())
                            .type(p.getType())
                            .children(children)
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }
}
