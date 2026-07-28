package top.huanyu666.backend.modules.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 可发货明细 DTO —— 前端发货单创建时加载的商品行。
 */
@Data
@AllArgsConstructor
public class DeliverableItem {

    private Long orderItemId;
    private Long materialId;
    private String materialName;
    private BigDecimal orderQuantity;
    private BigDecimal deliverableQuantity;
    private BigDecimal price;
    private String unit;
}
