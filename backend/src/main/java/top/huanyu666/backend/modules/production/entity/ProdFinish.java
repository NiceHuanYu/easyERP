package top.huanyu666.backend.modules.production.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

import java.time.LocalDate;

/**
 * 完工入库单主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_prod_finish")
public class ProdFinish extends BaseEntity {

    private String finishNo;

    private Long orderId;

    private Long warehouseId;

    private LocalDate finishDate;

    private String status;

    private String remark;
}
