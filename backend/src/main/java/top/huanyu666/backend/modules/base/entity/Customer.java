package top.huanyu666.backend.modules.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

/**
 * 客户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_base_customer")
public class Customer extends BaseEntity {

    /**
     * 客户编码
     */
    private String code;

    /**
     * 客户名称
     */
    private String name;

    /**
     * 联系人
     */
    private String contact;

    /**
     * 电话
     */
    private String phone;

    /**
     * 地址
     */
    private String address;

    private Integer status;
}
