package top.huanyu666.backend.modules.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存流水表
 */
@Data
@TableName("t_inv_transaction")
public class InvTransaction implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long materialId;

    private Long warehouseId;

    private String type;

    private BigDecimal quantity;

    private BigDecimal currentStock;

    private String sourceNo;

    private String sourceType;

    private LocalDateTime createTime;

    private Long createBy;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String materialName;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String warehouseName;
}
