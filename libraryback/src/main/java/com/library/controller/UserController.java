package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.User;
import com.library.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 * 处理用户的增删改查、状态更新和密码修改功能
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 分页查询用户列表
     * @param page 页码，默认1
     * @param size 每页大小，默认10
     * @param keyword 搜索关键词（用户名、真实姓名、手机号）
     * @return 分页后的用户列表
     */
    @GetMapping("/page")
    public Result<PageResult<User>> getUserPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        log.info("查询用户列表，页码：{}，每页大小：{}，关键词：{}", page, size, keyword);
        PageResult<User> result = userService.getUserPage(page, size, keyword);
        return Result.success(result);
    }

    /**
     * 根据ID查询用户信息
     * @param id 用户ID
     * @return 用户信息（密码字段置空）
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        log.info("查询用户信息，用户ID：{}", id);
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        }
        log.warn("用户不存在，用户ID：{}", id);
        return Result.error("用户不存在");
    }

    /**
     * 管理员添加用户
     * @param user 用户信息
     * @return 添加结果
     */
    @PostMapping
    public Result<Void> addUser(@RequestBody User user) {
        log.info("管理员添加用户，用户名：{}", user.getUsername());
        boolean success = userService.addUser(user);
        if (success) {
            log.info("用户添加成功，用户名：{}", user.getUsername());
            return Result.success("添加成功");
        }
        log.error("用户添加失败，用户名：{}", user.getUsername());
        return Result.error("添加失败");
    }

    /**
     * 更新用户信息
     * @param id 用户ID
     * @param user 更新的用户信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        log.info("更新用户信息，用户ID：{}", id);
        user.setId(id);
        boolean success = userService.updateById(user);
        if (success) {
            log.info("用户信息更新成功，用户ID：{}", id);
            return Result.success("更新成功");
        }
        log.error("用户信息更新失败，用户ID：{}", id);
        return Result.error("更新失败");
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        log.info("删除用户，用户ID：{}", id);
        boolean success = userService.removeById(id);
        if (success) {
            log.info("用户删除成功，用户ID：{}", id);
            return Result.success("删除成功");
        }
        log.error("用户删除失败，用户ID：{}", id);
        return Result.error("删除失败");
    }

    /**
     * 更新用户状态（启用/禁用）
     * @param id 用户ID
     * @param status 状态：0-禁用，1-启用
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        log.info("更新用户状态，用户ID：{}，状态：{}", id, status);
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        boolean success = userService.updateById(user);
        if (success) {
            log.info("用户状态更新成功，用户ID：{}", id);
            return Result.success("状态更新成功");
        }
        log.error("用户状态更新失败，用户ID：{}", id);
        return Result.error("状态更新失败");
    }

    /**
     * 修改用户密码
     * @param id 用户ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    @PostMapping("/{id}/password")
    public Result<Void> updatePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        log.info("修改用户密码，用户ID：{}", id);
        boolean success = userService.updatePassword(id, oldPassword, newPassword);
        if (success) {
            log.info("用户密码修改成功，用户ID：{}", id);
            return Result.success("密码修改成功");
        }
        log.error("用户密码修改失败，用户ID：{}", id);
        return Result.error("密码修改失败");
    }
}
