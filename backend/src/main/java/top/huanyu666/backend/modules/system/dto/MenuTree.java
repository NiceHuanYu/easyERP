package top.huanyu666.backend.modules.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜单树节点
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
