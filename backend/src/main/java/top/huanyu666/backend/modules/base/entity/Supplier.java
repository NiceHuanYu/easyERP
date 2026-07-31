package top.huanyu666.backend.modules.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

/**
 * 供应商
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_base_supplier")
public class Supplier extends BaseEntity {

    /**
     * 供应商编码
     */
    private String code;

    /**
     * 供应商名称
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
