package top.huanyu666.backend.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

/**
 * 数据字典明细
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_dict_item")
public class SysDictItem extends BaseEntity {

    private Long dictId;
    private String label;
    private String value;
    private Integer sort;
    private Integer status;
}
