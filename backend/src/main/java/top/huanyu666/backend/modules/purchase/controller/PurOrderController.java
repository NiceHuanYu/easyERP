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
import top.huanyu666.backend.modules.purchase.service.PurOrderService;
import top.huanyu666.backend.modules.base.entity.Supplier;
import top.huanyu666.backend.modules.base.mapper.SupplierMapper;

import java.util.List;
import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 采购订单管理
 */
@RestController
@RequestMapping("/api/v1/purchase/orders")
@RequiredArgsConstructor
@Slf4j
public class PurOrderController {

    private final PurOrderMapper orderMapper;
    private final PurOrderService orderService;
    private final SupplierMapper supplierMapper;

    // ==================== 基础 CRUD ====================

    @SaCheckPermission("purchase:order:view")
    @GetMapping
    public ApiResponse<PageResult<PurOrder>> list(PageParam param,
                                                   @RequestParam(required = false) Long supplierId,
                                                   @RequestParam(required = false) String status) {
        LambdaQueryWrapper<PurOrder> qw = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            qw.eq(PurOrder::getSupplierId, supplierId);
        }
        if (status != null && !status.isBlank()) {
            qw.eq(PurOrder::getStatus, status);
        }
        qw.orderByDesc(PurOrder::getCreateTime);
        Page<PurOrder> page = orderMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        page.getRecords().forEach(o -> {
            if (o.getSupplierId() != null) {
                Supplier s = supplierMapper.selectById(o.getSupplierId());
                o.setSupplierName(s != null ? s.getName() : "");
            }
        });
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("purchase:order:view")
    @GetMapping("/{id}")
    public ApiResponse<PurOrder> detail(@PathVariable Long id) {
        PurOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }
        return ApiResponse.ok(order);
    }

    @SaCheckPermission("purchase:order:create")
    @PostMapping
    public ApiResponse<PurOrder> create(@RequestBody PurOrder order) {
        order.setStatus("DRAFT");
        if (order.getOrderNo() == null || order.getOrderNo().isBlank()) {
            order.setOrderNo(CodeGenerator.generate("PO", () -> {
                PurOrder last = orderMapper.selectOne(
                        new LambdaQueryWrapper<PurOrder>()
                                .select(PurOrder::getOrderNo)
                                .orderByDesc(PurOrder::getOrderNo)
                                .last("LIMIT 1"));
                return last != null ? last.getOrderNo() : null;
            }));
        }
        orderMapper.insert(order);
        return ApiResponse.ok(order);
    }

    // ==================== 业务操作 ====================

    @SaCheckPermission("purchase:order:create")
    @PostMapping("/create-receiving/{id}")
    @Transactional
    public ApiResponse<PurReceiving> createReceiving(@PathVariable Long id,
                                                      @RequestBody PurReceiving receiving) {
        return ApiResponse.ok(orderService.createReceiving(id, receiving));
    }

    // ==================== 编辑/删除/下达 ====================

    @SaCheckPermission("purchase:order:create")
    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody PurOrder order) {
        PurOrder exist = orderMapper.selectById(id);
        if (exist == null) throw new BusinessException("采购订单不存在");
        if (!"DRAFT".equals(exist.getStatus())) throw new BusinessException("只有草稿状态可编辑");
        order.setId(id);
        orderMapper.updateById(order);
        return ApiResponse.ok();
    }

    @SaCheckPermission("purchase:order:create")
    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ApiResponse.ok();
    }

    @SaCheckPermission("purchase:order:create")
    @PostMapping("/{id}/issue")
    @Transactional
    public ApiResponse<Void> issue(@PathVariable Long id) {
        orderService.issue(id);
        return ApiResponse.ok();
    }
}
