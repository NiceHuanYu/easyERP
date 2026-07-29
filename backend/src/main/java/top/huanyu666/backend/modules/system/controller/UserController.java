package top.huanyu666.backend.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import cn.hutool.crypto.digest.BCrypt;
import top.huanyu666.backend.common.exception.BusinessException;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.PageParam;
import top.huanyu666.backend.common.model.PageResult;
import top.huanyu666.backend.modules.system.entity.SysUser;
import top.huanyu666.backend.modules.system.mapper.SysUserMapper;

import java.util.Map;

/**
 * 用户管理
 */
@RestController
@RequestMapping("/api/v1/system/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserMapper userMapper;

    @GetMapping
    @SaCheckPermission("system:user:view")
    public ApiResponse<PageResult<SysUser>> list(PageParam param,
                                                  @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(SysUser::getUsername, keyword).or().like(SysUser::getNickname, keyword));
        }
        qw.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> page = userMapper.selectPage(
                new Page<>(param.getPage(), param.getSize()), qw);
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
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        userMapper.insert(user);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:user:edit")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody SysUser user) {
        // 如果传入了密码则加密，否则保持原密码不变
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        } else {
            // 不传密码时，保留数据库中的原密码
            SysUser exist = userMapper.selectById(id);
            if (exist != null) {
                user.setPassword(exist.getPassword());
            }
        }
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

    @PutMapping("/{id}/status")
    @SaCheckPermission("system:user:edit")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus(body.get("status"));
        userMapper.updateById(user);
        return ApiResponse.ok();
    }
}
