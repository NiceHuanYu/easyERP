package top.huanyu666.backend.modules.production.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

import java.time.LocalDate;

/**
 * 领料单主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_prod_picking")
public class ProdPicking extends BaseEntity {

    private String pickingNo;

    private Long orderId;

    private Long warehouseId;

    private LocalDate pickingDate;

    private String status;

    private String remark;
}
