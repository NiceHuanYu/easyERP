package top.huanyu666.backend.modules.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收付款核销明细
 */
@Data
@TableName("t_fin_payment_item")
public class FinPaymentItem implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long paymentId;

    private Long receivableId;

    private Long payableId;

    private BigDecimal amount;

    private LocalDateTime createTime;
}
