package top.huanyu666.backend.modules.purchase.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import top.huanyu666.backend.modules.finance.service.FinPayableService;
import top.huanyu666.backend.modules.inventory.service.InvStockService;
import top.huanyu666.backend.modules.purchase.entity.*;
import top.huanyu666.backend.modules.purchase.mapper.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 收货单管理
 */
@RestController
@RequestMapping("/api/v1/purchase/receivings")
@RequiredArgsConstructor
@Slf4j
public class PurReceivingController {

    private final PurReceivingMapper receivingMapper;
    private final PurReceivingItemMapper receivingItemMapper;
    private final PurOrderMapper orderMapper;
    private final PurOrderItemMapper orderItemMapper;
    private final InvStockService stockService;
    private final FinPayableService payableService;

    // ==================== 基础 CRUD ====================

    @SaCheckPermission("purchase:order:view")
    @GetMapping
    public ApiResponse<PageResult<PurReceiving>> list(PageParam param,
                                                       @RequestParam(required = false) Long orderId,
                                                       @RequestParam(required = false) String status) {
        LambdaQueryWrapper<PurReceiving> qw = new LambdaQueryWrapper<>();
        if (orderId != null) {
            qw.eq(PurReceiving::getOrderId, orderId);
        }
        if (status != null && !status.isBlank()) {
            qw.eq(PurReceiving::getStatus, status);
        }
        qw.orderByDesc(PurReceiving::getCreateTime);
        Page<PurReceiving> page = receivingMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("purchase:order:create")
    @PostMapping
    public ApiResponse<PurReceiving> create(@RequestBody PurReceiving receiving) {
        receiving.setStatus("DRAFT");
        receivingMapper.insert(receiving);
        return ApiResponse.ok(receiving);
    }

    @SaCheckPermission("purchase:order:create")
    @PutMapping("/{id}")
    public ApiResponse<PurReceiving> update(@PathVariable Long id, @RequestBody PurReceiving receiving) {
        PurReceiving exist = receivingMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("收货单不存在");
        }
        if (!"DRAFT".equals(exist.getStatus())) {
            throw new BusinessException("仅草稿状态可修改");
        }
        receiving.setId(id);
        receivingMapper.updateById(receiving);
        return ApiResponse.ok(receivingMapper.selectById(id));
    }

    // ==================== 业务操作 ====================

    /**
     * 确认收货：DRAFT → CONFIRMED，增加库存，记录流水，创建应付
     */
    @SaCheckPermission("purchase:order:approve")
    @PostMapping("/confirm/{id}")
    @Transactional
    public ApiResponse<String> confirm(@PathVariable Long id) {
        PurReceiving receiving = receivingMapper.selectById(id);
        if (receiving == null) {
            throw new BusinessException("收货单不存在");
        }
        if (!"DRAFT".equals(receiving.getStatus())) {
            throw new BusinessException("仅草稿状态可确认");
        }

        // 查收货明细
        LambdaQueryWrapper<PurReceivingItem> itemQw = new LambdaQueryWrapper<>();
        itemQw.eq(PurReceivingItem::getReceivingId, id);
        List<PurReceivingItem> items = receivingItemMapper.selectList(itemQw);

        if (items.isEmpty()) {
            throw new BusinessException("收货单无明细");
        }

        PurOrder order = orderMapper.selectById(receiving.getOrderId());
        BigDecimal totalPayable = BigDecimal.ZERO;

        for (PurReceivingItem item : items) {
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("收货明细数量必须大于0");
            }

            // 增加库存（统一由 InvStockService 处理）
            stockService.receive(item.getMaterialId(), receiving.getWarehouseId(),
                    item.getQuantity(), receiving.getReceivingNo(), "PURCHASE_IN");

            // 更新订单明细已收数量
            if (item.getOrderItemId() != null) {
                PurOrderItem orderItem = orderItemMapper.selectById(item.getOrderItemId());
                if (orderItem != null) {
                    BigDecimal currentReceived = orderItem.getReceivedQty() != null
                            ? orderItem.getReceivedQty() : BigDecimal.ZERO;
                    orderItem.setReceivedQty(currentReceived.add(item.getQuantity()));
                    orderItem.setUpdateTime(LocalDateTime.now());
                    orderItemMapper.updateById(orderItem);

                    // 计算应付金额
                    if (orderItem.getPrice() != null) {
                        totalPayable = totalPayable.add(
                                orderItem.getPrice().multiply(item.getQuantity()));
                    }
                }
            }
        }

        // 创建应付台账
        if (order != null && totalPayable.compareTo(BigDecimal.ZERO) > 0) {
            payableService.createFromReceiving(id, order.getSupplierId(), totalPayable);
        }

        receiving.setStatus("CONFIRMED");
        receivingMapper.updateById(receiving);

        log.info("收货单 {} 已确认，{} 条明细，应付金额 {}", receiving.getReceivingNo(), items.size(), totalPayable);
        return ApiResponse.ok("确认成功");
    }
}
