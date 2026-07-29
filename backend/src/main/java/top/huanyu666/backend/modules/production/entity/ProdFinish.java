package top.huanyu666.backend.modules.production.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

import java.time.LocalDate;

/**
 * 完工入库单主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_prod_finish")
public class ProdFinish extends BaseEntity {

    private String finishNo;

    private Long orderId;

    private Long warehouseId;

    private LocalDate finishDate;

    private String status;

    private String remark;

    /** 以下为前端列表展示用的 transient 字段 */

    @TableField(exist = false)
    private String finishingNo;    // 前端字段名（含 ing）

    /** 工单号（列表展示用，非 DB 字段） */
    @TableField(exist = false)
    private String orderNo;

    @TableField(exist = false)
    private String finishingDate;  // 前端字段名（含 ing）

    @TableField(exist = false)
    private String materialName;

    @TableField(exist = false)
    private java.math.BigDecimal quantity;
}
