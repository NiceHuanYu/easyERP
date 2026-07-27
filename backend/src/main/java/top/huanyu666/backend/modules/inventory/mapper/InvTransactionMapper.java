package top.huanyu666.backend.modules.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.huanyu666.backend.modules.inventory.entity.InvTransaction;

/**
 * 库存流水 Mapper
 */
@Mapper
public interface InvTransactionMapper extends BaseMapper<InvTransaction> {
}
