package top.huanyu666.backend.modules.production.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.huanyu666.backend.modules.production.entity.ProdFinishItem;

/**
 * 完工入库明细 Mapper
 */
@Mapper
public interface ProdFinishItemMapper extends BaseMapper<ProdFinishItem> {
}
