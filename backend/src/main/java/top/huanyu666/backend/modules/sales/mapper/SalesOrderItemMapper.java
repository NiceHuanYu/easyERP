package top.huanyu666.backend.modules.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.huanyu666.backend.modules.sales.entity.SalesOrderItem;

/**
 * 销售订单明细 Mapper
 */
@Mapper
public interface SalesOrderItemMapper extends BaseMapper<SalesOrderItem> {
}
