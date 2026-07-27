package top.huanyu666.backend.modules.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.huanyu666.backend.modules.base.entity.Warehouse;

/**
 * 仓库 Mapper
 */
@Mapper
public interface WarehouseMapper extends BaseMapper<Warehouse> {
}
