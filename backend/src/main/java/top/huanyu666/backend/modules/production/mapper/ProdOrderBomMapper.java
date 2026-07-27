package top.huanyu666.backend.modules.production.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.huanyu666.backend.modules.production.entity.ProdOrderBom;

/**
 * 工单物料需求 Mapper
 */
@Mapper
public interface ProdOrderBomMapper extends BaseMapper<ProdOrderBom> {
}
