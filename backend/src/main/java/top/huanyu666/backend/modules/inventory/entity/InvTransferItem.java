package top.huanyu666.backend.modules.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_inv_transfer_item")
public class InvTransferItem implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long transferId;
    private Long materialId;
    private BigDecimal quantity;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String materialName;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String unit;
}
