package top.huanyu666.backend.modules.production.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.huanyu666.backend.modules.production.entity.ProdPickingItem;

/**
 * 领料单明细 Mapper
 */
@Mapper
public interface ProdPickingItemMapper extends BaseMapper<ProdPickingItem> {
}
