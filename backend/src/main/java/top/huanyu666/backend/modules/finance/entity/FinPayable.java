package top.huanyu666.backend.modules.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

import java.math.BigDecimal;

/**
 * 应付台账
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_fin_payable")
public class FinPayable extends BaseEntity {

    private Long receivingId;

    private Long supplierId;

    private BigDecimal payableAmount;

    private BigDecimal paidAmount;

    private String status;
}
