package top.huanyu666.backend.common.config;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限加载（从数据库加载用户的角色和权限码）
 * 一期简化：返回空集合，待角色权限表数据准备好后扩展
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // TODO: 查询数据库，返回用户拥有的权限码集合
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // TODO: 查询数据库，返回用户拥有的角色编码集合
        return Collections.emptyList();
    }
}
