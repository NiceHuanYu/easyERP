package top.huanyu666.backend.modules.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

import java.math.BigDecimal;

/**
 * BOM
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_base_bom")
public class Bom extends BaseEntity {

    /**
     * 父物料ID
     */
    private Long parentMaterialId;

    /**
     * 子物料ID
     */
    private Long childMaterialId;

    /**
     * 用量
     */
    private BigDecimal quantity;

    /**
     * 损耗率
     */
    private BigDecimal lossRate;

    /**
     * 排序
     */
    private Integer sort;
}
