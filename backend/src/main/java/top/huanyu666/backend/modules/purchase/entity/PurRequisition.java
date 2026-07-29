package top.huanyu666.backend.modules.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

import java.time.LocalDate;

/**
 * 采购申请主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_pur_requisition")
public class PurRequisition extends BaseEntity {

    private String requisitionNo;

    private String status;

    private String remark;

    private Long applicantId;
    private LocalDate reqDate;
}
