package top.huanyu666.backend.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

/**
 * 系统用户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_user")
public class SysUser extends BaseEntity {

    private String username;
    private String password;
    private String nickname;
    private Long employeeId;
    private Integer status;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String employeeName;
}
