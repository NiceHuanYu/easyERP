package top.huanyu666.backend.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import top.huanyu666.backend.modules.system.entity.SysUser;
import top.huanyu666.backend.modules.system.mapper.SysUserMapper;

/**
 * 用户管理
 */
@RestController
@RequestMapping("/api/v1/system/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserMapper userMapper;

    @GetMapping
    @SaCheckPermission("system:user:list")
    public ApiResponse<PageResult<SysUser>> list(PageParam param) {
        Page<SysUser> page = userMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()),
                new LambdaQueryWrapper<SysUser>().orderByDesc(SysUser::getCreateTime)
        );
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:user:view")
    public ApiResponse<SysUser> getById(@PathVariable Long id) {
        return ApiResponse.ok(userMapper.selectById(id));
    }

    @PostMapping
    @SaCheckPermission("system:user:create")
    public ApiResponse<Void> create(@RequestBody SysUser user) {
        userMapper.insert(user);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:user:update")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        userMapper.updateById(user);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:user:delete")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userMapper.deleteById(id);
        return ApiResponse.ok();
    }
}
