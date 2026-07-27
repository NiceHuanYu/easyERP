package top.huanyu666.backend.modules.production.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.huanyu666.backend.modules.production.entity.ProdPicking;

/**
 * 领料单 Mapper
 */
@Mapper
public interface ProdPickingMapper extends BaseMapper<ProdPicking> {
}
