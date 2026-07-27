package top.huanyu666.backend.modules.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

import java.time.LocalDate;

/**
 * 收货单主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_pur_receiving")
public class PurReceiving extends BaseEntity {

    private String receivingNo;

    private Long orderId;

    private Long warehouseId;

    private LocalDate receivingDate;

    private String status;

    private String remark;
}
