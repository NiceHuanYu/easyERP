package top.huanyu666.backend.modules.inventory.controller;

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
import top.huanyu666.backend.common.utils.CodeGenerator;
import top.huanyu666.backend.modules.base.entity.Material;
import top.huanyu666.backend.modules.base.entity.Warehouse;
import top.huanyu666.backend.modules.base.mapper.MaterialMapper;
import top.huanyu666.backend.modules.base.mapper.WarehouseMapper;
import top.huanyu666.backend.modules.inventory.entity.InvTransfer;
import top.huanyu666.backend.modules.inventory.entity.InvTransferItem;
import top.huanyu666.backend.modules.inventory.mapper.InvTransferItemMapper;
import top.huanyu666.backend.modules.inventory.mapper.InvTransferMapper;
import top.huanyu666.backend.modules.inventory.service.InvStockService;

import java.util.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/inventory/transfers")
@RequiredArgsConstructor
@Slf4j
public class InvTransferController {

    private final InvTransferMapper transferMapper;
    private final InvTransferItemMapper itemMapper;
    private final WarehouseMapper warehouseMapper;
    private final MaterialMapper materialMapper;
    private final InvStockService invStockService;

    @GetMapping
    @SaCheckPermission("inventory:stock:view")
    public ApiResponse<PageResult<InvTransfer>> list(PageParam param,
                                                      @RequestParam(required = false) String status) {
        LambdaQueryWrapper<InvTransfer> qw = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) qw.eq(InvTransfer::getStatus, status);
        qw.orderByDesc(InvTransfer::getCreateTime);
        Page<InvTransfer> page = transferMapper.selectPage(new Page<>(param.getPage(), param.getSize()), qw);
        if (!page.getRecords().isEmpty()) {
            List<Long> whIds = new ArrayList<>();
            page.getRecords().forEach(t -> {
                whIds.add(t.getFromWarehouseId());
                whIds.add(t.getToWarehouseId());
            });
            Map<Long, String> whNameMap = warehouseMapper.selectBatchIds(whIds.stream().distinct().toList())
                    .stream().collect(java.util.stream.Collectors.toMap(Warehouse::getId, Warehouse::getName, (a, b) -> a));
            page.getRecords().forEach(t -> {
                t.setFromWarehouseName(whNameMap.getOrDefault(t.getFromWarehouseId(), ""));
                t.setToWarehouseName(whNameMap.getOrDefault(t.getToWarehouseId(), ""));
            });
        }
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("inventory:stock:view")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        InvTransfer t = transferMapper.selectById(id);
        if (t == null) throw new BusinessException("调拨单不存在");
        List<InvTransferItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<InvTransferItem>().eq(InvTransferItem::getTransferId, id));
        if (!items.isEmpty()) {
            Map<Long, Material> matMap = materialMapper.selectBatchIds(
                    items.stream().map(InvTransferItem::getMaterialId).distinct().toList())
                    .stream().collect(java.util.stream.Collectors.toMap(Material::getId, m -> m, (a, b) -> a));
            items.forEach(i -> {
                Material m = matMap.get(i.getMaterialId());
                if (m != null) { i.setMaterialName(m.getName()); i.setUnit(m.getUnit()); }
            });
        }
        Map<String, Object> result = new HashMap<>();
        result.put("transfer", t);
        result.put("items", items);
        return ApiResponse.ok(result);
    }

    @PostMapping
    @SaCheckPermission("inventory:stock:view")
    @Transactional
    public ApiResponse<InvTransfer> create(@RequestBody Map<String, Object> body) {
        InvTransfer t = new InvTransfer();
        t.setFromWarehouseId(Long.valueOf(body.get("fromWarehouseId").toString()));
        t.setToWarehouseId(Long.valueOf(body.get("toWarehouseId").toString()));
        t.setStatus("DRAFT");
        t.setRemark((String) body.get("remark"));
        t.setTransferNo(CodeGenerator.generate("TR", () -> {
            InvTransfer last = transferMapper.selectOne(new LambdaQueryWrapper<InvTransfer>()
                    .select(InvTransfer::getTransferNo).orderByDesc(InvTransfer::getTransferNo).last("LIMIT 1"));
            return last != null ? last.getTransferNo() : null;
        }));
        transferMapper.insert(t);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lines = (List<Map<String, Object>>) body.get("lines");
        if (lines != null) saveItems(t.getId(), lines);
        return ApiResponse.ok(t);
    }

    @PostMapping("/{id}/confirm")
    @SaCheckPermission("inventory:stock:view")
    @Transactional
    public ApiResponse<Void> confirm(@PathVariable Long id) {
        InvTransfer t = transferMapper.selectById(id);
        if (t == null) throw new BusinessException("调拨单不存在");
        if (!"DRAFT".equals(t.getStatus())) throw new BusinessException("仅草稿状态可确认");
        List<InvTransferItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<InvTransferItem>().eq(InvTransferItem::getTransferId, id));
        if (items.isEmpty()) throw new BusinessException("无调拨明细");
        for (InvTransferItem item : items) {
            invStockService.transfer(item.getMaterialId(), t.getFromWarehouseId(), t.getToWarehouseId(), item.getQuantity(), t.getTransferNo());
        }
        t.setStatus("CONFIRMED");
        transferMapper.updateById(t);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("inventory:stock:view")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        InvTransfer t = transferMapper.selectById(id);
        if (t != null && !"DRAFT".equals(t.getStatus())) throw new BusinessException("仅草稿状态可删除");
        itemMapper.delete(new LambdaQueryWrapper<InvTransferItem>().eq(InvTransferItem::getTransferId, id));
        transferMapper.deleteById(id);
        return ApiResponse.ok();
    }

    private void saveItems(Long transferId, List<Map<String, Object>> lines) {
        for (Map<String, Object> line : lines) {
            InvTransferItem item = new InvTransferItem();
            item.setTransferId(transferId);
            item.setMaterialId(Long.valueOf(line.get("materialId").toString()));
            item.setQuantity(new BigDecimal(line.get("quantity").toString()));
            itemMapper.insert(item);
        }
    }
}
