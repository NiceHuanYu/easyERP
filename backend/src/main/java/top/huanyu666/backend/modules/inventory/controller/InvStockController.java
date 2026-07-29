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
import top.huanyu666.backend.modules.base.entity.Material;
import top.huanyu666.backend.modules.base.mapper.MaterialMapper;
import top.huanyu666.backend.modules.inventory.entity.InvStock;
import top.huanyu666.backend.modules.inventory.mapper.InvStockMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 库存管理
 */
@RestController
@RequestMapping("/api/v1/inventory/stock")
@RequiredArgsConstructor
@Slf4j
public class InvStockController {

    private final InvStockMapper stockMapper;
    private final MaterialMapper materialMapper;

    @SaCheckPermission("inventory:stock:view")
    @GetMapping("/warning-count")
    public ApiResponse<Long> warningCount() {
        // 统计库存数量低于安全库存的物料数（JOIN 查询，避免 N+1）
        LambdaQueryWrapper<InvStock> qw = new LambdaQueryWrapper<>();
        qw.isNotNull(InvStock::getQuantity);
        List<InvStock> stocks = stockMapper.selectList(qw);
        if (stocks.isEmpty()) return ApiResponse.ok(0L);
        // 批量查物料
        List<Long> materialIds = stocks.stream().map(InvStock::getMaterialId).distinct().toList();
        Map<Long, Material> materialMap = materialMapper.selectBatchIds(materialIds).stream()
                .collect(java.util.stream.Collectors.toMap(Material::getId, m -> m));
        long count = stocks.stream().filter(stock -> {
            Material m = materialMap.get(stock.getMaterialId());
            return m != null && m.getSafetyStock() != null
                    && stock.getQuantity().compareTo(m.getSafetyStock()) <= 0;
        }).count();
        return ApiResponse.ok(count);
    }

    @SaCheckPermission("inventory:stock:view")
    @GetMapping
    public ApiResponse<PageResult<InvStock>> list(PageParam param,
                                                   @RequestParam(required = false) Long materialId,
                                                   @RequestParam(required = false) Long warehouseId,
                                                   @RequestParam(required = false) BigDecimal minQuantity) {
        LambdaQueryWrapper<InvStock> qw = new LambdaQueryWrapper<>();
        if (materialId != null) {
            qw.eq(InvStock::getMaterialId, materialId);
        }
        if (warehouseId != null) {
            qw.eq(InvStock::getWarehouseId, warehouseId);
        }
        if (minQuantity != null) {
            qw.gt(InvStock::getQuantity, minQuantity);
        }
        qw.orderByDesc(InvStock::getCreateTime);
        Page<InvStock> page = stockMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        // 批量查物料名，避免 N+1
        if (!page.getRecords().isEmpty()) {
            List<Long> materialIds = page.getRecords().stream().map(InvStock::getMaterialId).distinct().toList();
            Map<Long, String> nameMap = materialMapper.selectBatchIds(materialIds).stream()
                    .collect(java.util.stream.Collectors.toMap(Material::getId, Material::getName, (a, b) -> a));
            page.getRecords().forEach(s -> s.setMaterialName(nameMap.getOrDefault(s.getMaterialId(), "")));
        }
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }
}
