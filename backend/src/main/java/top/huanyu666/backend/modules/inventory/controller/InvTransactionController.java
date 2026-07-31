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
import top.huanyu666.backend.modules.base.entity.Warehouse;
import top.huanyu666.backend.modules.base.mapper.MaterialMapper;
import top.huanyu666.backend.modules.base.mapper.WarehouseMapper;

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
    private final WarehouseMapper warehouseMapper;

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
            // 批量查仓库名
            List<Long> whIds = page.getRecords().stream().map(InvTransaction::getWarehouseId).distinct().toList();
            java.util.Map<Long, String> whNameMap = warehouseMapper.selectBatchIds(whIds).stream()
                    .collect(java.util.stream.Collectors.toMap(Warehouse::getId, Warehouse::getName, (a, b) -> a));
            page.getRecords().forEach(t -> {
                t.setMaterialName(nameMap.getOrDefault(t.getMaterialId(), ""));
                t.setWarehouseName(whNameMap.getOrDefault(t.getWarehouseId(), ""));
            });
        }
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @SaCheckPermission("inventory:stock:export")
    @GetMapping("/export")
    public void export(@RequestParam(required = false) Long materialId,
                       @RequestParam(required = false) Long warehouseId,
                       @RequestParam(required = false) String type,
                       jakarta.servlet.http.HttpServletResponse response) throws Exception {
        LambdaQueryWrapper<InvTransaction> qw = new LambdaQueryWrapper<>();
        if (materialId != null) qw.eq(InvTransaction::getMaterialId, materialId);
        if (warehouseId != null) qw.eq(InvTransaction::getWarehouseId, warehouseId);
        if (type != null && !type.isBlank()) qw.eq(InvTransaction::getType, type);
        qw.orderByDesc(InvTransaction::getCreateTime);
        List<InvTransaction> list = transactionMapper.selectList(qw);
        // 批量查物料名和仓库名
        java.util.Map<Long, String> nameMap = java.util.Collections.emptyMap();
        java.util.Map<Long, String> whNameMap = java.util.Collections.emptyMap();
        if (!list.isEmpty()) {
            List<Long> mIds = list.stream().map(InvTransaction::getMaterialId).distinct().toList();
            nameMap = materialMapper.selectBatchIds(mIds).stream()
                    .collect(java.util.stream.Collectors.toMap(Material::getId, Material::getName, (a, b) -> a));
            List<Long> wIds = list.stream().map(InvTransaction::getWarehouseId).distinct().toList();
            whNameMap = warehouseMapper.selectBatchIds(wIds).stream()
                    .collect(java.util.stream.Collectors.toMap(Warehouse::getId, Warehouse::getName, (a, b) -> a));
        }
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=inventory_transactions.csv");
        java.io.PrintWriter w = response.getWriter();
        w.println("日期,物料名称,仓库,变动类型,数量,当前库存,来源单号");
        for (InvTransaction t : list) {
            w.printf("\"%s\",\"%s\",\"%s\",%s,%.2f,%.2f,\"%s\"\n",
                    t.getCreateTime(), nameMap.getOrDefault(t.getMaterialId(), ""),
                    whNameMap.getOrDefault(t.getWarehouseId(), ""), t.getType(),
                    t.getQuantity(), t.getCurrentStock(), t.getSourceNo() != null ? t.getSourceNo() : "");
        }
        w.flush();
    }
}
