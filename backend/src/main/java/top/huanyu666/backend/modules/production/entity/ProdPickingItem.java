package top.huanyu666.backend.modules.production.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 领料单明细
 */
@Data
@TableName("t_prod_picking_item")
public class ProdPickingItem implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long pickingId;

    private Long materialId;

    private BigDecimal requestQty;

    private BigDecimal actualQty;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
