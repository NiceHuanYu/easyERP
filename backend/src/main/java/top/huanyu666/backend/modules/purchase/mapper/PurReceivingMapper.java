package top.huanyu666.backend.modules.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.huanyu666.backend.modules.purchase.entity.PurReceiving;

/**
 * 收货单 Mapper
 */
@Mapper
public interface PurReceivingMapper extends BaseMapper<PurReceiving> {
}
