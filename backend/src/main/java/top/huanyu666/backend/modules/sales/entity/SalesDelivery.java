package top.huanyu666.backend.modules.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

import java.time.LocalDate;

/**
 * 发货单主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sales_delivery")
public class SalesDelivery extends BaseEntity {

    /**
     * 发货单号
     */
    private String deliveryNo;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 发货日期
     */
    private LocalDate deliveryDate;

    /**
     * 发货状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /** 关联查询字段（非表字段，列表展示用） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String orderNo;
}
