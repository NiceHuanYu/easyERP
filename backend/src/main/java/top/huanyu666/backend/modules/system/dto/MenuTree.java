package top.huanyu666.backend.modules.system.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 菜单树节点
 */
@Data
@Builder
public class MenuTree {

    private Long id;
    private Long parentId;
    private String name;
    private String code;
    private String path;
    private String icon;
    private Integer type;
    private List<MenuTree> children;
}
