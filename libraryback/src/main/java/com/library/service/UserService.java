package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.User;

/**
 * 用户服务接口
 * 定义用户相关的业务操作方法
 */
public interface UserService extends IService<User> {
    
    /**
     * 用户登录验证
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回用户信息，失败抛出异常
     */
    User login(String username, String password);
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    User getByUsername(String username);
    
    /**
     * 分页查询用户列表
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词（用户名、真实姓名、手机号）
     * @return 分页后的用户列表
     */
    PageResult<User> getUserPage(Integer page, Integer size, String keyword);
    
    /**
     * 用户注册
     * @param user 用户注册信息
     * @return 注册是否成功
     */
    boolean register(User user);
    
    /**
     * 管理员添加用户
     * @param user 用户信息
     * @return 添加是否成功
     */
    boolean addUser(User user);
    
    /**
     * 修改用户密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改是否成功
     */
    boolean updatePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 获取用户总数
     * @return 用户总数
     */
    Long getUserCount();
}
