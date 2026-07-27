package top.huanyu666.backend.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

/**
 * 权限（菜单/按钮/接口）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_permission")
public class SysPermission extends BaseEntity {

    private Long parentId;
    private String name;
    private String code;
    private Integer type;
    private String path;
    private String icon;
    private Integer sort;
    private Integer status;
}
