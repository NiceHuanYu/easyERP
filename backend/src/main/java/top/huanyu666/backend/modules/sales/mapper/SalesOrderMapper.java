package top.huanyu666.backend.modules.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.huanyu666.backend.modules.sales.entity.SalesOrder;

/**
 * 销售订单 Mapper
 */
@Mapper
public interface SalesOrderMapper extends BaseMapper<SalesOrder> {
}
