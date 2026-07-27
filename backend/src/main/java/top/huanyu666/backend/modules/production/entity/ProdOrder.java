package top.huanyu666.backend.modules.production.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 生产工单主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_prod_order")
public class ProdOrder extends BaseEntity {

    private String orderNo;

    private Long salesOrderId;

    private Long materialId;

    private BigDecimal planQuantity;

    private BigDecimal finishQuantity;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;

    private String remark;
}
