package top.huanyu666.backend.modules.finance.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.modules.finance.entity.FinPayable;
import top.huanyu666.backend.modules.finance.mapper.FinPayableMapper;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinPayableService 应付服务单元测试")
class FinPayableServiceTest {

    @Mock
    private FinPayableMapper payableMapper;

    @InjectMocks
    private FinPayableService service;

    // ======================== createFromReceiving ========================

    @Nested
    @DisplayName("createFromReceiving — 收货时创建应付")
    class CreateFromReceiving {

        @Test
        @DisplayName("正常创建：状态 UNPAID，已付金额 0")
        void shouldCreateWithUnpaidStatus() {
            ArgumentCaptor<FinPayable> captor = ArgumentCaptor.forClass(FinPayable.class);

            service.createFromReceiving(100L, 200L, new BigDecimal("3000"));

            verify(payableMapper).insert(captor.capture());
            FinPayable p = captor.getValue();
            assertThat(p.getReceivingId()).isEqualTo(100L);
            assertThat(p.getSupplierId()).isEqualTo(200L);
            assertThat(p.getPayableAmount()).isEqualByComparingTo("3000");
            assertThat(p.getPaidAmount()).isEqualByComparingTo("0");
            assertThat(p.getStatus()).isEqualTo("UNPAID");
        }
    }

    // ======================== applyPayment ========================

    @Nested
    @DisplayName("applyPayment — 付款核销")
    class ApplyPayment {

        @Test
        @DisplayName("部分付款 → 状态 PARTIALLY_PAID")
        void shouldSetPartiallyPaid() {
            FinPayable p = payable(10000, 0);
            when(payableMapper.selectById(1L)).thenReturn(p);

            service.applyPayment(1L, new BigDecimal("4000"));

            assertThat(p.getPaidAmount()).isEqualByComparingTo("4000");
            assertThat(p.getStatus()).isEqualTo("PARTIALLY_PAID");
            verify(payableMapper).updateById(p);
        }

        @Test
        @DisplayName("全部付款 → 状态 FULLY_PAID")
        void shouldSetFullyPaid() {
            FinPayable p = payable(5000, 0);
            when(payableMapper.selectById(1L)).thenReturn(p);

            service.applyPayment(1L, new BigDecimal("5000"));

            assertThat(p.getStatus()).isEqualTo("FULLY_PAID");
        }

        @Test
        @DisplayName("台账不存在 → 抛 BusinessException")
        void shouldThrowWhenNotFound() {
            when(payableMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> service.applyPayment(999L, BigDecimal.ONE))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("应付台账不存在");
        }
    }

    // ======================== helper ========================

    private FinPayable payable(long total, long paid) {
        FinPayable p = new FinPayable();
        p.setId(1L);
        p.setReceivingId(10L);
        p.setSupplierId(20L);
        p.setPayableAmount(new BigDecimal(total));
        p.setPaidAmount(new BigDecimal(paid));
        p.setStatus("UNPAID");
        return p;
    }
}
