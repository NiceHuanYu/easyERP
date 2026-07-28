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

import java.util.List;

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
        // 统计库存数量低于安全库存的物料数
        List<InvStock> stocks = stockMapper.selectList(null);
        long count = stocks.stream().filter(stock -> {
            if (stock.getQuantity() == null) return false;
            Material material = materialMapper.selectById(stock.getMaterialId());
            if (material == null || material.getSafetyStock() == null) return false;
            return stock.getQuantity().compareTo(material.getSafetyStock()) <= 0;
        }).count();
        return ApiResponse.ok(count);
    }

    @SaCheckPermission("inventory:stock:view")
    @GetMapping
    public ApiResponse<PageResult<InvStock>> list(PageParam param,
                                                   @RequestParam(required = false) Long materialId,
                                                   @RequestParam(required = false) Long warehouseId) {
        LambdaQueryWrapper<InvStock> qw = new LambdaQueryWrapper<>();
        if (materialId != null) {
            qw.eq(InvStock::getMaterialId, materialId);
        }
        if (warehouseId != null) {
            qw.eq(InvStock::getWarehouseId, warehouseId);
        }
        qw.orderByDesc(InvStock::getCreateTime);
        Page<InvStock> page = stockMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
        page.getRecords().forEach(s -> {
            Material m = materialMapper.selectById(s.getMaterialId());
            s.setMaterialName(m != null ? m.getName() : "");
        });
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }
}
