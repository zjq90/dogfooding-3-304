package com.library.controller;

import com.library.common.Result;
import com.library.entity.User;
import com.library.service.UserService;
import com.library.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、注册、获取用户信息和登出功能
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录结果，包含token和用户信息
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @RequestParam @NotBlank(message = "用户名不能为空") String username,
            @RequestParam @NotBlank(message = "密码不能为空") String password) {
        
        log.info("用户登录: {}", username);
        
        User user = userService.login(username, password);
        
        // 生成JWT token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("role", user.getRole());
        result.put("avatar", user.getAvatar());
        
        return Result.success("登录成功", result);
    }

    /**
     * 用户注册
     * @param user 用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        log.info("用户注册: {}", user.getUsername());
        
        boolean success = userService.register(user);
        if (success) {
            return Result.success("注册成功");
        }
        return Result.error("注册失败");
    }

    /**
     * 获取当前登录用户信息
     * @param userId 当前登录用户ID（从JWT拦截器注入）
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestAttribute(name = "userId") Long userId) {
        log.info("获取用户信息，用户ID: {}", userId);
        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        }
        log.warn("用户不存在，用户ID: {}", userId);
        return Result.error("用户不存在");
    }

    /**
     * 用户登出
     * @param userId 当前登录用户ID（从JWT拦截器注入）
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestAttribute(name = "userId") Long userId) {
        log.info("用户登出，用户ID: {}", userId);
        return Result.success("登出成功");
    }
}
