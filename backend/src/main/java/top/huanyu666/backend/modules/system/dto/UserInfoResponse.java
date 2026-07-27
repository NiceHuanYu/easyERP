package top.huanyu666.backend.modules.system.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 用户信息响应（含权限码和菜单）
 */
@Data
@Builder
public class UserInfoResponse {

    private Long userId;
    private String username;
    private String nickname;
    private List<String> permissions;
    private List<MenuTree> menus;
}
