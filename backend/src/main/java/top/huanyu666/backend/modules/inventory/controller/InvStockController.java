package top.huanyu666.backend.modules.inventory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import top.huanyu666.backend.modules.inventory.entity.InvStock;
import top.huanyu666.backend.modules.inventory.mapper.InvStockMapper;

/**
 * 库存管理
 */
@RestController
@RequestMapping("/api/v1/inventory/stock")
@RequiredArgsConstructor
@Slf4j
public class InvStockController {

    private final InvStockMapper stockMapper;

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
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }
}
