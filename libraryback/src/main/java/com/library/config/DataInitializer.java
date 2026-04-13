package com.library.config;

import com.library.entity.User;
import com.library.mapper.UserMapper;
import com.library.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 在应用启动时初始化默认数据
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void run(String... args) throws Exception {
        initAdminUser();
    }

    /**
     * 初始化管理员用户
     * 如果管理员用户不存在，则创建默认管理员
     */
    private void initAdminUser() {
        User existUser = userMapper.selectByUsername("admin");
        if (existUser == null) {
            log.info("初始化管理员用户...");
            
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(PasswordUtil.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setRole(1);
            admin.setStatus(1);
            
            userMapper.insert(admin);
            log.info("管理员用户初始化完成，用户名: admin, 密码: admin123");
        } else {
            log.info("管理员用户已存在，跳过初始化");
        }
    }
}
