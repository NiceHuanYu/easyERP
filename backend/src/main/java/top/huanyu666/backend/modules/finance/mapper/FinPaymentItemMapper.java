package top.huanyu666.backend.modules.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.huanyu666.backend.modules.finance.entity.FinPaymentItem;

/**
 * 收付款核销明细 Mapper
 */
@Mapper
public interface FinPaymentItemMapper extends BaseMapper<FinPaymentItem> {
}
