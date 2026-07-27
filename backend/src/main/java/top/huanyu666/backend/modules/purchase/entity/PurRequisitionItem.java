package top.huanyu666.backend.modules.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购申请明细
 */
@Data
@TableName("t_pur_requisition_item")
public class PurRequisitionItem implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long requisitionId;

    private Long materialId;

    private BigDecimal quantity;

    private BigDecimal orderedQty;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
