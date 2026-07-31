package top.huanyu666.backend.modules.purchase.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.utils.CodeGenerator;
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

    @SaCheckPermission("purchase:order:view")
    @GetMapping
    public ApiResponse<PageResult<PurRequisition>> list(PageParam param,
                                                         @RequestParam(required = false) String reqNo,
                                                         @RequestParam(required = false) String status) {
        LambdaQueryWrapper<PurRequisition> qw = new LambdaQueryWrapper<>();
        if (reqNo != null && !reqNo.isBlank()) qw.like(PurRequisition::getRequisitionNo, reqNo);
        if (status != null && !status.isBlank()) {
            qw.eq(PurRequisition::getStatus, status);
        }
        qw.orderByDesc(PurRequisition::getCreateTime);
        Page<PurRequisition> page = requisitionMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    /** 详情（含明细） */
    @SaCheckPermission("purchase:order:view")
    @GetMapping("/{id}")
    public ApiResponse<java.util.Map<String, Object>> detail(@PathVariable Long id) {
        PurRequisition req = requisitionMapper.selectById(id);
        if (req == null) throw new BusinessException("采购申请不存在");
        List<PurRequisitionItem> items = requisitionItemMapper.selectList(
                new LambdaQueryWrapper<PurRequisitionItem>().eq(PurRequisitionItem::getRequisitionId, id));
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", req.getId());
        result.put("reqNo", req.getRequisitionNo());
        result.put("status", req.getStatus());
        result.put("remark", req.getRemark());
        result.put("applicantId", req.getApplicantId());
        result.put("reqDate", req.getReqDate() != null ? req.getReqDate().toString() : "");
        result.put("lines", items.stream().map(i -> {
            java.util.Map<String, Object> line = new java.util.HashMap<>();
            line.put("materialId", i.getMaterialId());
            line.put("quantity", i.getQuantity());
            line.put("orderedQty", i.getOrderedQty());
            return line;
        }).toList());
        return ApiResponse.ok(result);
    }

    @SaCheckPermission("purchase:order:create")
    @PostMapping
    public ApiResponse<PurRequisition> create(@RequestBody java.util.Map<String, Object> body) {
        PurRequisition requisition = mapToRequisition(body);
        if (requisition.getStatus() == null || requisition.getStatus().isBlank()) {
            requisition.setStatus("DRAFT");
        }
        if (requisition.getRequisitionNo() == null || requisition.getRequisitionNo().isBlank()) {
            requisition.setRequisitionNo(CodeGenerator.generate("PR", () -> {
                PurRequisition last = requisitionMapper.selectOne(
                        new LambdaQueryWrapper<PurRequisition>()
                                .select(PurRequisition::getRequisitionNo)
                                .orderByDesc(PurRequisition::getRequisitionNo)
                                .last("LIMIT 1"));
                return last != null ? last.getRequisitionNo() : null;
            }));
        }
        requisitionMapper.insert(requisition);
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> lines = (java.util.List<java.util.Map<String, Object>>) body.get("lines");
        if (lines != null) saveRequisitionItems(requisition.getId(), lines);
        return ApiResponse.ok(requisition);
    }

    @SaCheckPermission("purchase:order:create")
    @PutMapping("/{id}")
    public ApiResponse<PurRequisition> update(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        PurRequisition exist = requisitionMapper.selectById(id);
        if (exist == null) throw new BusinessException("采购申请不存在");
        if (!"DRAFT".equals(exist.getStatus())) throw new BusinessException("仅草稿状态可修改");
        PurRequisition requisition = mapToRequisition(body);
        requisition.setId(id);
        requisitionMapper.updateById(requisition);
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> lines = (java.util.List<java.util.Map<String, Object>>) body.get("lines");
        if (lines != null) {
            requisitionItemMapper.delete(new LambdaQueryWrapper<PurRequisitionItem>().eq(PurRequisitionItem::getRequisitionId, id));
            saveRequisitionItems(id, lines);
        }
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
        if (!"APPROVED".equals(requisition.getStatus())) {
            throw new BusinessException("仅已审核的申请可生成订单");
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
            BigDecimal currentOrdered = reqItem.getOrderedQty() != null
                    ? reqItem.getOrderedQty() : BigDecimal.ZERO;
            reqItem.setOrderedQty(currentOrdered.add(reqItem.getQuantity()));
            reqItem.setUpdateTime(LocalDateTime.now());
            requisitionItemMapper.updateById(reqItem);
        }

        // 更新申请状态：全部明细都已下单才设为 ORDERED
        List<PurRequisitionItem> allItems = requisitionItemMapper.selectList(
                new LambdaQueryWrapper<PurRequisitionItem>().eq(PurRequisitionItem::getRequisitionId, id));
        boolean allOrdered = allItems.stream().allMatch(item ->
                item.getOrderedQty() != null && item.getOrderedQty().compareTo(item.getQuantity()) >= 0);
        requisition.setStatus(allOrdered ? "ORDERED" : "APPROVED");
        requisitionMapper.updateById(requisition);

        log.info("采购申请 {} 生成采购订单 {}", requisition.getRequisitionNo(), order.getOrderNo());
        return ApiResponse.ok(order);
    }

    // ==================== 状态流转 ====================

    /** 提交：DRAFT → SUBMITTED */
    @SaCheckPermission("purchase:order:create")
    @PostMapping("/{id}/submit")
    @Transactional
    public ApiResponse<Void> submit(@PathVariable Long id) {
        PurRequisition r = requisitionMapper.selectById(id);
        if (r == null) throw new BusinessException("采购申请不存在");
        if (!"DRAFT".equals(r.getStatus())) throw new BusinessException("仅草稿状态可提交");
        r.setStatus("SUBMITTED");
        requisitionMapper.updateById(r);
        log.info("采购申请 {} 已提交", r.getRequisitionNo());
        return ApiResponse.ok();
    }

    /** 审核通过：SUBMITTED → APPROVED */
    @SaCheckPermission("purchase:order:create")
    @PostMapping("/{id}/approve")
    @Transactional
    public ApiResponse<Void> approve(@PathVariable Long id) {
        PurRequisition r = requisitionMapper.selectById(id);
        if (r == null) throw new BusinessException("采购申请不存在");
        if (!"SUBMITTED".equals(r.getStatus())) throw new BusinessException("仅已提交状态可审核");
        r.setStatus("APPROVED");
        requisitionMapper.updateById(r);
        log.info("采购申请 {} 已审核通过", r.getRequisitionNo());
        return ApiResponse.ok();
    }

    // ==================== 删除 ====================

    @SaCheckPermission("purchase:order:create")
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

    private PurRequisition mapToRequisition(java.util.Map<String, Object> body) {
        PurRequisition r = new PurRequisition();
        if (body.containsKey("id")) r.setId(Long.valueOf(body.get("id").toString()));
        if (body.containsKey("reqNo")) r.setRequisitionNo((String) body.get("reqNo"));
        if (body.containsKey("status")) r.setStatus((String) body.get("status"));
        if (body.containsKey("remark")) r.setRemark((String) body.get("remark"));
        if (body.containsKey("applicantId")) r.setApplicantId(Long.valueOf(body.get("applicantId").toString()));
        if (body.containsKey("reqDate") && body.get("reqDate") != null && !body.get("reqDate").toString().isBlank()) r.setReqDate(LocalDate.parse(body.get("reqDate").toString()));
        return r;
    }

    private void saveRequisitionItems(Long reqId, java.util.List<java.util.Map<String, Object>> lines) {
        for (java.util.Map<String, Object> line : lines) {
            PurRequisitionItem item = new PurRequisitionItem();
            item.setRequisitionId(reqId);
            if (line.containsKey("materialId") && line.get("materialId") != null)
                item.setMaterialId(Long.valueOf(line.get("materialId").toString()));
            if (line.containsKey("quantity") && line.get("quantity") != null)
                item.setQuantity(new BigDecimal(line.get("quantity").toString()));
            item.setOrderedQty(BigDecimal.ZERO);
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            requisitionItemMapper.insert(item);
        }
    }
}
