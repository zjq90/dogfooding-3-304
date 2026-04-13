package com.library;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 图书借阅管理系统启动类
 * 主入口，配置组件扫描和缓存
 */
@SpringBootApplication
@MapperScan("com.library.mapper")
@EnableCaching
public class LibrarySystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibrarySystemApplication.class, args);
        System.out.println("========================================");
        System.out.println("    图书借阅管理系统启动成功！");
        System.out.println("    访问地址: http://localhost:8080/api");
        System.out.println("========================================");
    }
}
