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
import top.huanyu666.backend.modules.purchase.entity.*;
import top.huanyu666.backend.modules.purchase.mapper.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 采购申请管理
 */
@RestController
@RequestMapping("/api/v1/purchase/requisitions")
@RequiredArgsConstructor
@Slf4j
public class PurRequisitionController {

    private final PurRequisitionMapper requisitionMapper;
    private final PurRequisitionItemMapper requisitionItemMapper;
    private final PurOrderMapper orderMapper;
    private final PurOrderItemMapper orderItemMapper;

    // ==================== 基础 CRUD ====================

    @SaCheckPermission("purchase:requisition:list")
    @GetMapping
    public ApiResponse<PageResult<PurRequisition>> list(PageParam param,
                                                         @RequestParam(required = false) String status) {
        LambdaQueryWrapper<PurRequisition> qw = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            qw.eq(PurRequisition::getStatus, status);
        }
        qw.orderByDesc(PurRequisition::getCreateTime);
        Page<PurRequisition> page = requisitionMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("purchase:requisition:create")
    @PostMapping
    public ApiResponse<PurRequisition> create(@RequestBody PurRequisition requisition) {
        requisition.setStatus("DRAFT");
        requisitionMapper.insert(requisition);
        return ApiResponse.ok(requisition);
    }

    @SaCheckPermission("purchase:requisition:create")
    @PutMapping("/{id}")
    public ApiResponse<PurRequisition> update(@PathVariable Long id, @RequestBody PurRequisition requisition) {
        PurRequisition exist = requisitionMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("采购申请不存在");
        }
        if (!"DRAFT".equals(exist.getStatus())) {
            throw new BusinessException("仅草稿状态可修改");
        }
        requisition.setId(id);
        requisitionMapper.updateById(requisition);
        return ApiResponse.ok(requisitionMapper.selectById(id));
    }

    // ==================== 业务操作 ====================

    /**
     * 生成采购订单
     */
    @SaCheckPermission("purchase:order:create")
    @PostMapping("/create-order/{id}")
    @Transactional
    public ApiResponse<PurOrder> createOrder(@PathVariable Long id,
                                              @RequestBody PurOrder order) {
        PurRequisition requisition = requisitionMapper.selectById(id);
        if (requisition == null) {
            throw new BusinessException("采购申请不存在");
        }

        // 查申请明细
        LambdaQueryWrapper<PurRequisitionItem> itemQw = new LambdaQueryWrapper<>();
        itemQw.eq(PurRequisitionItem::getRequisitionId, id);
        List<PurRequisitionItem> reqItems = requisitionItemMapper.selectList(itemQw);

        if (reqItems.isEmpty()) {
            throw new BusinessException("采购申请无明细");
        }

        // 创建采购订单
        order.setStatus("DRAFT");
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(BigDecimal.ZERO);
        orderMapper.insert(order);

        // 创建订单明细
        int lineNo = 1;
        for (PurRequisitionItem reqItem : reqItems) {
            PurOrderItem orderItem = new PurOrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setLineNo(lineNo++);
            orderItem.setMaterialId(reqItem.getMaterialId());
            orderItem.setQuantity(reqItem.getQuantity());
            orderItem.setReceivedQty(BigDecimal.ZERO);
            orderItem.setCreateTime(LocalDateTime.now());
            orderItem.setUpdateTime(LocalDateTime.now());
            orderItemMapper.insert(orderItem);

            // 更新申请明细已下单数量
            reqItem.setOrderedQty(
                    reqItem.getOrderedQty() != null
                            ? reqItem.getOrderedQty().add(reqItem.getQuantity())
                            : reqItem.getQuantity());
            reqItem.setUpdateTime(LocalDateTime.now());
            requisitionItemMapper.updateById(reqItem);
        }

        // 更新申请状态
        requisition.setStatus("ORDERED");
        requisitionMapper.updateById(requisition);

        log.info("采购申请 {} 生成采购订单 {}", requisition.getRequisitionNo(), order.getOrderNo());
        return ApiResponse.ok(order);
    }

    // ==================== 删除 ====================

    @SaCheckPermission("purchase:requisition:create")
    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        PurRequisition r = requisitionMapper.selectById(id);
        if (r == null) throw new BusinessException("采购申请不存在");
        if (!"DRAFT".equals(r.getStatus())) throw new BusinessException("只有草稿状态可删除");
        requisitionItemMapper.delete(new LambdaQueryWrapper<PurRequisitionItem>().eq(PurRequisitionItem::getRequisitionId, id));
        requisitionMapper.deleteById(id);
        return ApiResponse.ok();
    }
}
