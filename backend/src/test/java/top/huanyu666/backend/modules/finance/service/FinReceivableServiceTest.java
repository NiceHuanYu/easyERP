package top.huanyu666.backend.modules.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.modules.finance.entity.FinReceivable;
import top.huanyu666.backend.modules.finance.mapper.FinReceivableMapper;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinReceivableService 应收服务单元测试")
class FinReceivableServiceTest {

    @Mock
    private FinReceivableMapper receivableMapper;

    @InjectMocks
    private FinReceivableService service;

    // ======================== createFromDelivery ========================

    @Nested
    @DisplayName("createFromDelivery — 发货时创建应收")
    class CreateFromDelivery {

        @Test
        @DisplayName("正常创建：状态 PENDING，已收金额 0")
        void shouldCreateWithPendingStatus() {
            ArgumentCaptor<FinReceivable> captor = ArgumentCaptor.forClass(FinReceivable.class);

            service.createFromDelivery(100L, 200L, new BigDecimal("5000"));

            verify(receivableMapper).insert(captor.capture());
            FinReceivable r = captor.getValue();
            assertThat(r.getDeliveryId()).isEqualTo(100L);
            assertThat(r.getCustomerId()).isEqualTo(200L);
            assertThat(r.getReceivableAmount()).isEqualByComparingTo("5000");
            assertThat(r.getReceivedAmount()).isEqualByComparingTo("0");
            assertThat(r.getStatus()).isEqualTo("PENDING");
        }
    }

    // ======================== applyPayment ========================

    @Nested
    @DisplayName("applyPayment — 收款核销")
    class ApplyPayment {

        @Test
        @DisplayName("部分收款 → 状态 PARTIALLY_PAID")
        void shouldSetPartiallyPaid() {
            FinReceivable r = receivable(10000, 0);
            when(receivableMapper.selectById(1L)).thenReturn(r);

            service.applyPayment(1L, new BigDecimal("3000"));

            assertThat(r.getReceivedAmount()).isEqualByComparingTo("3000");
            assertThat(r.getStatus()).isEqualTo("PARTIALLY_PAID");
            verify(receivableMapper).updateById(r);
        }

        @Test
        @DisplayName("全部收款 → 状态 FULLY_PAID")
        void shouldSetFullyPaid() {
            FinReceivable r = receivable(10000, 5000);
            when(receivableMapper.selectById(1L)).thenReturn(r);

            service.applyPayment(1L, new BigDecimal("5000"));

            assertThat(r.getReceivedAmount()).isEqualByComparingTo("10000");
            assertThat(r.getStatus()).isEqualTo("FULLY_PAID");
        }

        @Test
        @DisplayName("超额收款 → 状态 FULLY_PAID")
        void shouldSetFullyPaidEvenWhenOverpaid() {
            FinReceivable r = receivable(10000, 9000);
            when(receivableMapper.selectById(1L)).thenReturn(r);

            service.applyPayment(1L, new BigDecimal("2000"));

            assertThat(r.getStatus()).isEqualTo("FULLY_PAID");
        }

        @Test
        @DisplayName("台账不存在 → 抛 BusinessException")
        void shouldThrowWhenNotFound() {
            when(receivableMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> service.applyPayment(999L, BigDecimal.ONE))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("应收台账不存在");
        }
    }

    // ======================== helper ========================

    private FinReceivable receivable(long total, long received) {
        FinReceivable r = new FinReceivable();
        r.setId(1L);
        r.setDeliveryId(10L);
        r.setCustomerId(20L);
        r.setReceivableAmount(new BigDecimal(total));
        r.setReceivedAmount(new BigDecimal(received));
        r.setStatus("PENDING");
        return r;
    }
}
