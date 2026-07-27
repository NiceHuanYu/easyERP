package top.huanyu666.backend.modules.production.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工单物料需求
 */
@Data
@TableName("t_prod_order_bom")
public class ProdOrderBom implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long orderId;

    private Long materialId;

    private BigDecimal requiredQty;

    private BigDecimal pickedQty;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
