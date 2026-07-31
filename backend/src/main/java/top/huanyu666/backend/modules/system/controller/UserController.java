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
import top.huanyu666.backend.modules.system.entity.SysUserRole;
import top.huanyu666.backend.modules.system.mapper.SysUserMapper;
import top.huanyu666.backend.modules.system.mapper.SysUserRoleMapper;
import top.huanyu666.backend.modules.base.entity.Employee;
import top.huanyu666.backend.modules.base.mapper.EmployeeMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理
 */
@RestController
@RequestMapping("/api/v1/system/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final EmployeeMapper employeeMapper;

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
        // 填充员工名称
        for (SysUser u : page.getRecords()) {
            if (u.getEmployeeId() != null) {
                Employee e = employeeMapper.selectById(u.getEmployeeId());
                u.setEmployeeName(e != null ? e.getName() : "");
            }
        }
        return ApiResponse.ok(new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:user:view")
    public ApiResponse<SysUser> getById(@PathVariable Long id) {
        return ApiResponse.ok(userMapper.selectById(id));
    }

    @PostMapping
    @SaCheckPermission("system:user:create")
    public ApiResponse<SysUser> create(@RequestBody SysUser user) {
        if (existsByUsername(user.getUsername())) {
            return ApiResponse.error("用户名已存在");
        }
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        userMapper.insert(user);
        return ApiResponse.ok(user);
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:user:edit")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody SysUser user) {
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            Long count = userMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername()).ne(SysUser::getId, id));
            if (count > 0) return ApiResponse.error("用户名已存在");
        }
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
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
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

    /** 获取用户角色ID列表 */
    @GetMapping("/{id}/roles")
    @SaCheckPermission("system:user:view")
    public ApiResponse<List<Long>> getUserRoles(@PathVariable Long id) {
        List<SysUserRole> list = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        return ApiResponse.ok(list.stream().map(SysUserRole::getRoleId).collect(Collectors.toList()));
    }

    /** 设置用户角色 */
    @PutMapping("/{id}/roles")
    @SaCheckPermission("system:user:edit")
    public ApiResponse<Void> setUserRoles(@PathVariable Long id, @RequestBody java.util.List<Object> rawRoleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        for (Object raw : rawRoleIds) {
            Long roleId = Long.valueOf(raw.toString());
            SysUserRole ur = new SysUserRole();
            ur.setUserId(id);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
        return ApiResponse.ok();
    }

    private boolean existsByUsername(String username) {
        return userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)) > 0;
    }
}
