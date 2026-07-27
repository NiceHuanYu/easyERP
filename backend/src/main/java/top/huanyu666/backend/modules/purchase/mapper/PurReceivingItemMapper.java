package top.huanyu666.backend.modules.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.huanyu666.backend.modules.purchase.entity.PurReceivingItem;

/**
 * 收货单明细 Mapper
 */
@Mapper
public interface PurReceivingItemMapper extends BaseMapper<PurReceivingItem> {
}
