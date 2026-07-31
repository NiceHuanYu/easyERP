package top.huanyu666.backend.modules.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.huanyu666.backend.common.model.BaseEntity;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_inv_transfer")
public class InvTransfer extends BaseEntity {

    private String transferNo;
    private Long fromWarehouseId;
    private Long toWarehouseId;
    private LocalDate transferDate;
    private String status;
    private String remark;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String fromWarehouseName;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String toWarehouseName;
}
