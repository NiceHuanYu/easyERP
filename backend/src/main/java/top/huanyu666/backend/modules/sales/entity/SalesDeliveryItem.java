package top.huanyu666.backend.modules.sales.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发货单明细表
 */
@Data
@TableName("t_sales_delivery_item")
public class SalesDeliveryItem implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 发货单ID
     */
    private Long deliveryId;

    /**
     * 订单明细ID
     */
    private Long orderItemId;

    /**
     * 物料ID
     */
    private Long materialId;

    /**
     * 发货数量
     */
    private BigDecimal quantity;
}
