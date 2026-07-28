package top.huanyu666.backend.modules.inventory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.modules.inventory.entity.InvStock;
import top.huanyu666.backend.modules.inventory.entity.InvTransaction;
import top.huanyu666.backend.modules.inventory.mapper.InvStockMapper;
import top.huanyu666.backend.modules.inventory.mapper.InvTransactionMapper;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvStockService 库存服务单元测试")
class InvStockServiceTest {

    @Mock
    private InvStockMapper stockMapper;

    @Mock
    private InvTransactionMapper transactionMapper;

    @InjectMocks
    private InvStockService service;

    private static final Long MATERIAL_ID = 1L;
    private static final Long WAREHOUSE_ID = 10L;
    private static final BigDecimal QTY_100 = new BigDecimal("100");
    private static final BigDecimal QTY_30 = new BigDecimal("30");
    private static final BigDecimal QTY_70 = new BigDecimal("70");

    // ======================== deduct 出库 ========================

    @Nested
    @DisplayName("deduct — 出库扣减")
    class Deduct {

        @Test
        @DisplayName("正常扣减 → 更新库存 + 插入负数量流水")
        void shouldDeductAndRecordTransaction() {
            InvStock stock = stockWith(QTY_100, QTY_100);
            when(stockMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(stock);

            service.deduct(MATERIAL_ID, WAREHOUSE_ID, QTY_30, "SO-001", "SALES_OUT");

            // 验证库存更新
            assertThat(stock.getAvailableQty()).isEqualByComparingTo(QTY_70);
            assertThat(stock.getQuantity()).isEqualByComparingTo(QTY_70);
            verify(stockMapper).updateById(stock);

            // 验证流水插入
            ArgumentCaptor<InvTransaction> txCaptor = ArgumentCaptor.forClass(InvTransaction.class);
            verify(transactionMapper).insert(txCaptor.capture());
            InvTransaction tx = txCaptor.getValue();
            assertThat(tx.getMaterialId()).isEqualTo(MATERIAL_ID);
            assertThat(tx.getWarehouseId()).isEqualTo(WAREHOUSE_ID);
            assertThat(tx.getType()).isEqualTo("SALES_OUT");
            assertThat(tx.getQuantity()).isEqualByComparingTo(QTY_30.negate());
            assertThat(tx.getCurrentStock()).isEqualByComparingTo(QTY_70);
            assertThat(tx.getSourceNo()).isEqualTo("SO-001");
        }

        @Test
        @DisplayName("库存记录不存在 → 抛 BusinessException")
        void shouldThrowWhenStockNotFound() {
            when(stockMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            assertThatThrownBy(() -> service.deduct(MATERIAL_ID, WAREHOUSE_ID, QTY_30, "X", "T"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("库存记录不存在");
        }

        @Test
        @DisplayName("库存不足 → 抛 BusinessException")
        void shouldThrowWhenInsufficientStock() {
            InvStock stock = stockWith(new BigDecimal("10"), new BigDecimal("10"));
            when(stockMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(stock);

            assertThatThrownBy(() -> service.deduct(MATERIAL_ID, WAREHOUSE_ID, QTY_100, "X", "T"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("库存不足");
        }

        @Test
        @DisplayName("availableQty 为 null → 抛异常")
        void shouldHandleNullAvailableQty() {
            InvStock stock = stockWith(QTY_100, null);
            when(stockMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(stock);

            // null < QTY_30 → NPE wrapped / or compareTo fails
            assertThatThrownBy(() -> service.deduct(MATERIAL_ID, WAREHOUSE_ID, QTY_30, "X", "T"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("库存不足");
        }
    }

    // ======================== receive 入库 ========================

    @Nested
    @DisplayName("receive — 入库增加")
    class Receive {

        @Test
        @DisplayName("库存已存在 → 累加 + 插入正数量流水")
        void shouldAddToExistingStock() {
            InvStock stock = stockWith(QTY_100, QTY_100);
            when(stockMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(stock);

            service.receive(MATERIAL_ID, WAREHOUSE_ID, QTY_30, "RCV-001", "PURCHASE_IN");

            assertThat(stock.getQuantity()).isEqualByComparingTo(new BigDecimal("130"));
            assertThat(stock.getAvailableQty()).isEqualByComparingTo(new BigDecimal("130"));
            verify(stockMapper).updateById(stock);
            verify(stockMapper, never()).insert(any());

            ArgumentCaptor<InvTransaction> txCaptor = ArgumentCaptor.forClass(InvTransaction.class);
            verify(transactionMapper).insert(txCaptor.capture());
            assertThat(txCaptor.getValue().getQuantity()).isEqualByComparingTo(QTY_30);
            assertThat(txCaptor.getValue().getType()).isEqualTo("PURCHASE_IN");
        }

        @Test
        @DisplayName("库存不存在 → 自动创建 + 插入流水")
        void shouldCreateStockWhenNotFound() {
            when(stockMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            service.receive(MATERIAL_ID, WAREHOUSE_ID, QTY_100, "RCV-002", "FINISH_IN");

            ArgumentCaptor<InvStock> stockCaptor = ArgumentCaptor.forClass(InvStock.class);
            verify(stockMapper).insert(stockCaptor.capture());
            InvStock created = stockCaptor.getValue();
            assertThat(created.getMaterialId()).isEqualTo(MATERIAL_ID);
            assertThat(created.getWarehouseId()).isEqualTo(WAREHOUSE_ID);
            assertThat(created.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO);

            verify(stockMapper).updateById(created);
            verify(transactionMapper).insert(any(InvTransaction.class));
        }
    }

    // ======================== helper ========================

    private InvStock stockWith(BigDecimal quantity, BigDecimal availableQty) {
        InvStock s = new InvStock();
        s.setId(1L);
        s.setMaterialId(MATERIAL_ID);
        s.setWarehouseId(WAREHOUSE_ID);
        s.setQuantity(quantity);
        s.setAvailableQty(availableQty);
        s.setLockedQty(BigDecimal.ZERO);
        return s;
    }
}
