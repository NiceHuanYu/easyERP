package top.huanyu666.backend.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
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
}
