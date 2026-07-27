package top.huanyu666.backend.modules.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收货单明细
 */
@Data
@TableName("t_pur_receiving_item")
public class PurReceivingItem implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long receivingId;

    private Long orderItemId;

    private Long materialId;

    private BigDecimal quantity;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
