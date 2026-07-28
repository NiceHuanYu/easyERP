package top.huanyu666.backend.modules.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购订单主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_pur_order")
public class PurOrder extends BaseEntity {

    private String orderNo;

    private Long supplierId;

    private LocalDate orderDate;

    private BigDecimal totalAmount;

    private String status;

    private String remark;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String supplierName;
}
