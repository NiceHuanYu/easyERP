package top.huanyu666.backend.modules.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

/**
 * 客户/供应商银行账户（支持多账户）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_counterparty_account")
public class CounterpartyAccount extends BaseEntity {

    private String ownerType;
    private Long ownerId;
    private String bankName;
    private String branchName;
    private String accountNo;
    private String accountName;
    private String currency;
    private String accountType;
    private Integer status;
    private String remark;

    /** 关联实体名称（列表展示用，非DB字段） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String ownerName;
}
