package top.huanyu666.backend.modules.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_bom_detail")
public class BomDetail implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long bomId;
    private Long materialId;
    private BigDecimal quantity;
    private String unit;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
