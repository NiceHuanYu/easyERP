package top.huanyu666.backend.modules.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

/**
 * 员工
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_base_employee")
public class Employee extends BaseEntity {

    /**
     * 员工编码
     */
    private String code;

    /**
     * 员工姓名
     */
    private String name;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 部门
     */
    private String dept;

    /**
     * 状态
     */
    private Integer status;
}
