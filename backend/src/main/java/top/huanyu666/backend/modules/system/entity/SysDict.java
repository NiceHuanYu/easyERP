package top.huanyu666.backend.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

/**
 * 数据字典类型
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_dict")
public class SysDict extends BaseEntity {

    private String name;
    private String code;
    private Integer status;
}
