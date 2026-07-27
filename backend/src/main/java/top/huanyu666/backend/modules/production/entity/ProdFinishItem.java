package top.huanyu666.backend.modules.production.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 完工入库明细
 */
@Data
@TableName("t_prod_finish_item")
public class ProdFinishItem implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long finishId;

    private Long materialId;

    private BigDecimal quantity;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
