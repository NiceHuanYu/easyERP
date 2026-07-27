package top.huanyu666.backend.modules.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.huanyu666.backend.modules.purchase.entity.PurOrder;

/**
 * 采购订单 Mapper
 */
@Mapper
public interface PurOrderMapper extends BaseMapper<PurOrder> {
}
