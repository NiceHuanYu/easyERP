package top.huanyu666.backend.modules.inventory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.modules.inventory.entity.InvStock;
import top.huanyu666.backend.modules.inventory.entity.InvTransaction;
import top.huanyu666.backend.modules.inventory.mapper.InvStockMapper;
import top.huanyu666.backend.modules.inventory.mapper.InvTransactionMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存服务 —— 封装所有库存增减操作与流水记录。
 * <p>
 * 调用方负责提供 {@code @Transactional} 事务边界，
 * 本 Service 方法本身不加事务注解以避免嵌套事务传播问题。
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvStockService {

    private final InvStockMapper stockMapper;
    private final InvTransactionMapper transactionMapper;

    /**
     * 出库扣减：检查可用库存 → reduce → update → insert 流水。
     *
     * @return 扣减后的库存记录
     * @throws BusinessException 库存记录不存在或可用量不足
     */
    public InvStock deduct(Long materialId, Long warehouseId, BigDecimal qty,
                           String sourceNo, String sourceType) {
        InvStock stock = stockMapper.selectOne(
                new LambdaQueryWrapper<InvStock>()
                        .eq(InvStock::getMaterialId, materialId)
                        .eq(InvStock::getWarehouseId, warehouseId));

        if (stock == null) {
            throw new BusinessException("库存记录不存在: materialId=" + materialId
                    + ", warehouseId=" + warehouseId);
        }
        if (stock.getAvailableQty() == null
                || stock.getAvailableQty().compareTo(qty) < 0) {
            throw new BusinessException("库存不足: materialId=" + materialId
                    + ", 可用=" + stock.getAvailableQty() + ", 需要=" + qty);
        }

        stock.setAvailableQty(stock.getAvailableQty().subtract(qty));
        stock.setQuantity(nvl(stock.getQuantity()).subtract(qty));
        stock.setUpdateTime(LocalDateTime.now());
        stockMapper.updateById(stock);

        insertTransaction(materialId, warehouseId, qty.negate(),
                stock.getQuantity(), sourceNo, sourceType);

        log.info("库存扣减: materialId={}, warehouseId={}, qty={}, 当前库存={}",
                materialId, warehouseId, qty, stock.getQuantity());
        return stock;
    }

    /**
     * 入库增加：库存不存在时自动创建 → add → update → insert 流水。
     *
     * @return 增加后的库存记录
     */
    public InvStock receive(Long materialId, Long warehouseId, BigDecimal qty,
                            String sourceNo, String sourceType) {
        InvStock stock = stockMapper.selectOne(
                new LambdaQueryWrapper<InvStock>()
                        .eq(InvStock::getMaterialId, materialId)
                        .eq(InvStock::getWarehouseId, warehouseId));

        if (stock == null) {
            stock = new InvStock();
            stock.setMaterialId(materialId);
            stock.setWarehouseId(warehouseId);
            stock.setQuantity(BigDecimal.ZERO);
            stock.setAvailableQty(BigDecimal.ZERO);
            stock.setLockedQty(BigDecimal.ZERO);
            stock.setCreateTime(LocalDateTime.now());
            stock.setUpdateTime(LocalDateTime.now());
            stockMapper.insert(stock);
        }

        stock.setQuantity(nvl(stock.getQuantity()).add(qty));
        stock.setAvailableQty(nvl(stock.getAvailableQty()).add(qty));
        stock.setUpdateTime(LocalDateTime.now());
        stockMapper.updateById(stock);

        insertTransaction(materialId, warehouseId, qty,
                stock.getQuantity(), sourceNo, sourceType);

        log.info("库存增加: materialId={}, warehouseId={}, qty={}, 当前库存={}",
                materialId, warehouseId, qty, stock.getQuantity());
        return stock;
    }

    // ---- private helpers ----

    private void insertTransaction(Long materialId, Long warehouseId,
                                   BigDecimal qty, BigDecimal currentStock,
                                   String sourceNo, String sourceType) {
        InvTransaction tx = new InvTransaction();
        tx.setMaterialId(materialId);
        tx.setWarehouseId(warehouseId);
        tx.setType(sourceType);
        tx.setQuantity(qty);
        tx.setCurrentStock(currentStock);
        tx.setSourceNo(sourceNo);
        tx.setSourceType(sourceType);
        tx.setCreateTime(LocalDateTime.now());
        transactionMapper.insert(tx);
    }

    private static BigDecimal nvl(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}
