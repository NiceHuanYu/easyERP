package top.huanyu666.backend.modules.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

/**
 * 公司银行账户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_company_account")
public class CompanyAccount extends BaseEntity {

    private String bankName;
    private String branchName;
    private String accountNo;
    private String accountName;
    private String currency;
    private Integer status;
    private String accountType;
    private String remark;
}
