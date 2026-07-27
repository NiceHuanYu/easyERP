package top.huanyu666.backend.modules.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.huanyu666.backend.modules.finance.entity.FinPayment;

/**
 * 收付款单 Mapper
 */
@Mapper
public interface FinPaymentMapper extends BaseMapper<FinPayment> {
}
