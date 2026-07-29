package top.huanyu666.backend.modules.inventory.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import top.huanyu666.backend.modules.inventory.entity.InvTransaction;
import top.huanyu666.backend.modules.inventory.mapper.InvTransactionMapper;
import top.huanyu666.backend.modules.base.entity.Material;
import top.huanyu666.backend.modules.base.mapper.MaterialMapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存流水管理
 */
@RestController
@RequestMapping("/api/v1/inventory/transactions")
@RequiredArgsConstructor
@Slf4j
public class InvTransactionController {

    private final InvTransactionMapper transactionMapper;
    private final MaterialMapper materialMapper;

    @SaCheckPermission("inventory:stock:view")
    @GetMapping
    public ApiResponse<PageResult<InvTransaction>> list(PageParam param,
                                                         @RequestParam(required = false) Long materialId,
                                                         @RequestParam(required = false) Long warehouseId,
                                                         @RequestParam(required = false) String type,
                                                         @RequestParam(required = false) LocalDateTime startTime,
                                                         @RequestParam(required = false) LocalDateTime endTime) {
        LambdaQueryWrapper<InvTransaction> qw = new LambdaQueryWrapper<>();
        if (materialId != null) {
            qw.eq(InvTransaction::getMaterialId, materialId);
        }
        if (warehouseId != null) {
            qw.eq(InvTransaction::getWarehouseId, warehouseId);
        }
        if (type != null) {
            qw.eq(InvTransaction::getType, type);
        }
        if (startTime != null) {
            qw.ge(InvTransaction::getCreateTime, startTime);
        }
        if (endTime != null) {
            qw.le(InvTransaction::getCreateTime, endTime);
        }
        qw.orderByDesc(InvTransaction::getCreateTime);
        Page<InvTransaction> page = transactionMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        // 批量查物料名，避免 N+1
        if (!page.getRecords().isEmpty()) {
            List<Long> materialIds = page.getRecords().stream().map(InvTransaction::getMaterialId).distinct().toList();
            java.util.Map<Long, String> nameMap = materialMapper.selectBatchIds(materialIds).stream()
                    .collect(java.util.stream.Collectors.toMap(Material::getId, Material::getName, (a, b) -> a));
            page.getRecords().forEach(t -> t.setMaterialName(nameMap.getOrDefault(t.getMaterialId(), "")));
        }
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }
}
