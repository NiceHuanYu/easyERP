package top.huanyu666.backend.modules.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 收付款单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_fin_payment")
public class FinPayment extends BaseEntity {

    private String paymentNo;

    private String type;

    private Long counterpartyId;

    private BigDecimal amount;

    private String bankAccount;

    private LocalDate paymentDate;

    private String status;

    private String remark;

    private java.math.BigDecimal reconciledAmount;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String counterpartyName;
}
