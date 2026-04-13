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
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @RequestParam @NotBlank(message = "用户名不能为空") String username,
            @RequestParam @NotBlank(message = "密码不能为空") String password) {

        log.info("用户登录请求，用户名: {}", username);

        User user = userService.login(username, password);
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("role", user.getRole());
        result.put("avatar", user.getAvatar());

        log.info("用户登录成功: {}", username);
        return Result.success("登录成功", result);
    }

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        log.info("用户注册请求: {}", user.getUsername());

        boolean success = userService.register(user);
        if (success) {
            log.info("用户注册成功: {}", user.getUsername());
            return Result.success("注册成功");
        }
        log.error("用户注册失败: {}", user.getUsername());
        return Result.error("注册失败");
    }

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestAttribute Long userId) {
        log.debug("获取用户信息: {}", userId);

        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }

    /**
     * 用户登出
     *
     * @param userId 用户ID
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestAttribute Long userId) {
        log.info("用户登出: {}", userId);
        return Result.success("登出成功");
    }
}
