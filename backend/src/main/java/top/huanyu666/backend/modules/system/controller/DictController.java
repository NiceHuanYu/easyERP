package top.huanyu666.backend.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.modules.system.entity.SysDict;
import top.huanyu666.backend.modules.system.entity.SysDictItem;
import top.huanyu666.backend.modules.system.mapper.SysDictItemMapper;
import top.huanyu666.backend.modules.system.mapper.SysDictMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据字典
 */
@RestController
@RequestMapping("/api/v1/system/dicts")
@RequiredArgsConstructor
public class DictController {

    private final SysDictMapper dictMapper;
    private final SysDictItemMapper dictItemMapper;

    @GetMapping
    public ApiResponse<List<SysDictItem>> getByCode(@RequestParam String code) {
        SysDict dict = dictMapper.selectOne(
                new LambdaQueryWrapper<SysDict>().eq(SysDict::getCode, code)
        );
        if (dict == null) {
            return ApiResponse.ok(List.of());
        }
        List<SysDictItem> items = dictItemMapper.selectList(
                new LambdaQueryWrapper<SysDictItem>()
                        .eq(SysDictItem::getDictId, dict.getId())
                        .eq(SysDictItem::getStatus, 1)
                        .orderByAsc(SysDictItem::getSort)
        );
        return ApiResponse.ok(items);
    }

    @GetMapping("/all")
    public ApiResponse<Map<String, List<SysDictItem>>> all() {
        List<SysDict> dicts = dictMapper.selectList(
                new LambdaQueryWrapper<SysDict>().eq(SysDict::getStatus, 1)
        );
        Map<String, List<SysDictItem>> result = new HashMap<>();
        for (SysDict dict : dicts) {
            List<SysDictItem> items = dictItemMapper.selectList(
                    new LambdaQueryWrapper<SysDictItem>()
                            .eq(SysDictItem::getDictId, dict.getId())
                            .eq(SysDictItem::getStatus, 1)
                            .orderByAsc(SysDictItem::getSort)
            );
            result.put(dict.getCode(), items);
        }
        return ApiResponse.ok(result);
    }

    @PostMapping("/types")
    public ApiResponse<SysDict> createType(@RequestBody SysDict dict) {
        dictMapper.insert(dict);
        return ApiResponse.ok(dict);
    }

    @PutMapping("/types/{id}")
    public ApiResponse<Void> updateType(@PathVariable Long id, @RequestBody SysDict dict) {
        dict.setId(id);
        dictMapper.updateById(dict);
        return ApiResponse.ok();
    }

    @DeleteMapping("/types/{id}")
    public ApiResponse<Void> deleteType(@PathVariable Long id) {
        dictMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{typeCode}/items")
    public ApiResponse<List<SysDictItem>> getItemsByTypeCode(@PathVariable String typeCode) {
        SysDict dict = dictMapper.selectOne(
                new LambdaQueryWrapper<SysDict>().eq(SysDict::getCode, typeCode)
        );
        if (dict == null) {
            return ApiResponse.ok(List.of());
        }
        List<SysDictItem> items = dictItemMapper.selectList(
                new LambdaQueryWrapper<SysDictItem>()
                        .eq(SysDictItem::getDictId, dict.getId())
                        .orderByAsc(SysDictItem::getSort)
        );
        return ApiResponse.ok(items);
    }

    @PostMapping("/{typeCode}/items")
    public ApiResponse<SysDictItem> createItem(@PathVariable String typeCode, @RequestBody SysDictItem item) {
        SysDict dict = dictMapper.selectOne(new LambdaQueryWrapper<SysDict>().eq(SysDict::getCode, typeCode));
        if (dict == null) throw new BusinessException("字典类型不存在");
        item.setDictId(dict.getId());
        dictItemMapper.insert(item);
        return ApiResponse.ok(item);
    }

    @PutMapping("/{typeCode}/items/{itemId}")
    public ApiResponse<Void> updateItem(@PathVariable String typeCode, @PathVariable Long itemId, @RequestBody SysDictItem item) {
        item.setId(itemId);
        dictItemMapper.updateById(item);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{typeCode}/items/{itemId}")
    public ApiResponse<Void> deleteItem(@PathVariable String typeCode, @PathVariable Long itemId) {
        dictItemMapper.deleteById(itemId);
        return ApiResponse.ok();
    }
}
