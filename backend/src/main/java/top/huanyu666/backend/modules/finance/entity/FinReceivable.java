package top.huanyu666.backend.modules.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 应收台账
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_fin_receivable")
public class FinReceivable extends BaseEntity {

    private Long deliveryId;

    private Long customerId;

    private BigDecimal receivableAmount;

    private BigDecimal receivedAmount;

    private String status;

    private LocalDate dueDate;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String customerName;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String deliveryNo;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String receivableNo;
}
