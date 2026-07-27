package top.huanyu666.backend.modules.production.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.huanyu666.backend.modules.production.entity.ProdFinish;

/**
 * 完工入库单 Mapper
 */
@Mapper
public interface ProdFinishMapper extends BaseMapper<ProdFinish> {
}
