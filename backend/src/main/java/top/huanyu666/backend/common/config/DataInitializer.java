package top.huanyu666.backend.common.config;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import top.huanyu666.backend.modules.system.entity.SysUser;
import top.huanyu666.backend.modules.system.mapper.SysUserMapper;

/**
 * 数据初始化：首次启动创建管理员账号
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final SysUserMapper userMapper;

    @Override
    public void run(ApplicationArguments args) {
        long count = userMapper.selectCount(new LambdaQueryWrapper<>());
        if (count > 0) {
            log.info("数据库已有用户，跳过初始化");
            return;
        }

        String passwordHash = BCrypt.hashpw("admin123");
        SysUser admin = new SysUser();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword(passwordHash);
        admin.setNickname("系统管理员");
        admin.setStatus(1);
        userMapper.insert(admin);

        log.info("管理员账号已创建: admin / admin123");
    }
}
