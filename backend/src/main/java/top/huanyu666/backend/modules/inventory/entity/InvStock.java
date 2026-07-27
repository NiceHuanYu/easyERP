package top.huanyu666.backend.modules.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存表
 */
@Data
@TableName("t_inv_stock")
public class InvStock implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long materialId;

    private Long warehouseId;

    private BigDecimal quantity;

    private BigDecimal availableQty;

    private BigDecimal lockedQty;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
