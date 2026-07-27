package top.huanyu666.backend.modules.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.huanyu666.backend.modules.base.entity.Material;

/**
 * 物料 Mapper
 */
@Mapper
public interface MaterialMapper extends BaseMapper<Material> {
}
