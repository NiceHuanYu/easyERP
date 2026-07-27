package top.huanyu666.backend.modules.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.huanyu666.backend.modules.inventory.entity.InvStock;

/**
 * 库存 Mapper
 */
@Mapper
public interface InvStockMapper extends BaseMapper<InvStock> {
}
