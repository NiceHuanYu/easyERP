package top.huanyu666.backend.modules.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_bom_header")
public class BomHeader extends BaseEntity {
    private String bomNo;
    private Long productMaterialId;
    private String version;
    private Integer status;
}
