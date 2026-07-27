package top.huanyu666.backend.modules.system.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
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
            throw new BusinessException("账号或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        StpUtil.login(user.getId());
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

        // 构建菜单树
        List<SysPermission> allPerms = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getStatus, 1)
                        .orderByAsc(SysPermission::getSort)
        );
        List<MenuTree> menus = buildMenuTree(allPerms, 0L);

        return UserInfoResponse.builder()
                .userId(userId)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .permissions(permissions)
                .menus(menus)
                .build();
    }

    private List<MenuTree> buildMenuTree(List<SysPermission> all, Long parentId) {
        return all.stream()
                .filter(p -> parentId.equals(p.getParentId()))
                .map(p -> MenuTree.builder()
                        .id(p.getId())
                        .parentId(p.getParentId())
                        .name(p.getName())
                        .code(p.getCode())
                        .path(p.getPath())
                        .icon(p.getIcon())
                        .type(p.getType())
                        .children(buildMenuTree(all, p.getId()))
                        .build())
                .collect(Collectors.toList());
    }
}
