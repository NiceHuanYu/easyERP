package top.huanyu666.backend.modules.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购订单明细
 */
@Data
@TableName("t_pur_order_item")
public class PurOrderItem implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long orderId;

    private Integer lineNo;

    private Long materialId;

    private BigDecimal quantity;

    private String unit;

    private BigDecimal price;

    private BigDecimal amount;

    private BigDecimal receivedQty;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
