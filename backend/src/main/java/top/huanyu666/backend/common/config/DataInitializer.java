package top.huanyu666.backend.common.config;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import top.huanyu666.backend.modules.system.entity.SysUser;
import top.huanyu666.backend.modules.system.entity.SysUserRole;
import top.huanyu666.backend.modules.system.mapper.SysUserMapper;
import top.huanyu666.backend.modules.system.mapper.SysUserRoleMapper;

/**
 * 数据初始化：根据配置决定是否创建管理员账号
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Value("${app.init.create-admin:true}")
    private boolean createAdmin;

    @Value("${app.init.admin-password:admin123}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        long count = userMapper.selectCount(new LambdaQueryWrapper<>());
        if (count > 0) {
            log.info("数据库已有用户，跳过初始化");
            return;
        }

        if (!createAdmin) {
            log.warn("app.init.create-admin=false，跳过管理员创建。"
                    + "请手动创建管理员账号后将开关设为 true，或直接设置 create-admin=true 自动创建");
            return;
        }

        String passwordHash = BCrypt.hashpw(adminPassword);
        SysUser admin = new SysUser();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword(passwordHash);
        admin.setNickname("系统管理员");
        admin.setStatus(1);
        userMapper.insert(admin);

        SysUserRole userRole = new SysUserRole();
        userRole.setId(1L);
        userRole.setUserId(1L);
        userRole.setRoleId(1L);
        userRoleMapper.insert(userRole);

        log.info("管理员账号已创建: admin，请立即修改密码");
    }
}
