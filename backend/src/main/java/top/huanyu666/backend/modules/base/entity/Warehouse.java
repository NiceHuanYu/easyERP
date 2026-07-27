package top.huanyu666.backend.modules.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

/**
 * 仓库
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_base_warehouse")
public class Warehouse extends BaseEntity {

    /**
     * 仓库编码
     */
    private String code;

    /**
     * 仓库名称
     */
    private String name;

    /**
     * 地址
     */
    private String address;

    /**
     * 状态
     */
    private Integer status;
}
