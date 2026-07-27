package top.huanyu666.backend.modules.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.huanyu666.backend.modules.finance.entity.FinPayable;

/**
 * 应付台账 Mapper
 */
@Mapper
public interface FinPayableMapper extends BaseMapper<FinPayable> {
}
