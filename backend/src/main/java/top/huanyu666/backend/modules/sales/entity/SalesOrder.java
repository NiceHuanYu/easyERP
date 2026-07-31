package top.huanyu666.backend.modules.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 销售订单主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sales_order")
public class SalesOrder extends BaseEntity {

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 订单日期
     */
    private LocalDate orderDate;

    /**
     * 交货日期
     */
    private LocalDate deliveryDate;

    private String customerPoNo;

    private BigDecimal totalAmount;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String customerName;
}
