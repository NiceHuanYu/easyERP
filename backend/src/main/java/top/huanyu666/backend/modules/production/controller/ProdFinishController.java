package top.huanyu666.backend.modules.production.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
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
import top.huanyu666.backend.modules.inventory.service.InvStockService;
import top.huanyu666.backend.modules.production.entity.*;
import top.huanyu666.backend.modules.production.mapper.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 完工入库单管理
 */
@RestController
@RequestMapping("/api/v1/production/finishings")
@RequiredArgsConstructor
@Slf4j
public class ProdFinishController {

    private final ProdFinishMapper finishMapper;
    private final ProdFinishItemMapper finishItemMapper;
    private final ProdOrderMapper orderMapper;
    private final InvStockService stockService;

    // ==================== 基础 CRUD ====================

    @SaCheckPermission("production:order:view")
    @GetMapping
    public ApiResponse<PageResult<ProdFinish>> list(PageParam param,
                                                     @RequestParam(required = false) Long orderId,
                                                     @RequestParam(required = false) String status) {
        LambdaQueryWrapper<ProdFinish> qw = new LambdaQueryWrapper<>();
        if (orderId != null) {
            qw.eq(ProdFinish::getOrderId, orderId);
        }
        if (status != null && !status.isBlank()) {
            qw.eq(ProdFinish::getStatus, status);
        }
        qw.orderByDesc(ProdFinish::getCreateTime);
        Page<ProdFinish> page = finishMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("production:order:create")
    @PostMapping
    public ApiResponse<ProdFinish> create(@RequestBody ProdFinish finish) {
        finish.setStatus("DRAFT");
        finishMapper.insert(finish);
        return ApiResponse.ok(finish);
    }

    @SaCheckPermission("production:order:create")
    @PutMapping("/{id}")
    public ApiResponse<ProdFinish> update(@PathVariable Long id, @RequestBody ProdFinish finish) {
        ProdFinish exist = finishMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("完工入库单不存在");
        }
        if (!"DRAFT".equals(exist.getStatus())) {
            throw new BusinessException("仅草稿状态可修改");
        }
        finish.setId(id);
        finishMapper.updateById(finish);
        return ApiResponse.ok(finishMapper.selectById(id));
    }

    // ==================== 业务操作 ====================

    /**
     * 确认完工入库：DRAFT → CONFIRMED，增加库存，记录流水
     */
    @SaCheckPermission("production:finish:confirm")
    @PostMapping("/confirm/{id}")
    @Transactional
    public ApiResponse<String> confirm(@PathVariable Long id) {
        ProdFinish finish = finishMapper.selectById(id);
        if (finish == null) {
            throw new BusinessException("完工入库单不存在");
        }
        if (!"DRAFT".equals(finish.getStatus())) {
            throw new BusinessException("仅草稿状态可确认");
        }

        // 查完工入库明细
        LambdaQueryWrapper<ProdFinishItem> itemQw = new LambdaQueryWrapper<>();
        itemQw.eq(ProdFinishItem::getFinishId, id);
        List<ProdFinishItem> items = finishItemMapper.selectList(itemQw);

        if (items.isEmpty()) {
            throw new BusinessException("完工入库单无明细");
        }

        for (ProdFinishItem item : items) {
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("完工入库明细数量必须大于0");
            }

            // 增加库存（统一由 InvStockService 处理）
            stockService.receive(item.getMaterialId(), finish.getWarehouseId(),
                    item.getQuantity(), finish.getFinishNo(), "FINISH_IN");
        }

        // 更新工单完工数量
        ProdOrder order = orderMapper.selectById(finish.getOrderId());
        if (order != null) {
            BigDecimal totalFinishQty = items.stream()
                    .map(ProdFinishItem::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal currentFinish = order.getFinishQuantity() != null
                    ? order.getFinishQuantity() : BigDecimal.ZERO;
            order.setFinishQuantity(currentFinish.add(totalFinishQty));
            orderMapper.updateById(order);
        }

        finish.setStatus("CONFIRMED");
        finishMapper.updateById(finish);

        log.info("完工入库单 {} 已确认，{} 条明细", finish.getFinishNo(), items.size());
        return ApiResponse.ok("确认成功");
    }

    /**
     * 删除（仅草稿）
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        ProdFinish f = finishMapper.selectById(id);
        if (f == null) throw new BusinessException("完工入库单不存在");
        if (!"DRAFT".equals(f.getStatus())) throw new BusinessException("只有草稿状态可删除");
        finishItemMapper.delete(new LambdaQueryWrapper<ProdFinishItem>().eq(ProdFinishItem::getFinishId, id));
        finishMapper.deleteById(id);
        return ApiResponse.ok();
    }
}
