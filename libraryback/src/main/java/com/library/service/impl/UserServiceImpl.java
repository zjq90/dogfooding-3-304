package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.BusinessException;
import com.library.common.PageResult;
import com.library.entity.User;
import com.library.mapper.UserMapper;
import com.library.service.UserService;
import com.library.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     * @throws BusinessException 登录失败时抛出
     */
    @Override
    public User login(String username, String password) {
        log.info("用户登录，用户名: {}", username);

        User user = baseMapper.selectByUsername(username);
        if (user == null) {
            log.warn("登录失败，用户不存在: {}", username);
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            log.warn("登录失败，账号已被禁用: {}", username);
            throw new BusinessException("账号已被禁用");
        }
        if (!PasswordUtil.matches(password, user.getPassword())) {
            log.warn("登录失败，密码错误: {}", username);
            throw new BusinessException("用户名或密码错误");
        }

        String cacheKey = buildUserCacheKey(user.getId());
        redisTemplate.opsForValue().set(cacheKey, user, 30, TimeUnit.MINUTES);
        log.info("用户登录成功，已缓存用户信息: {}", username);

        return user;
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    public User getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    /**
     * 分页查询用户列表
     *
     * @param page    页码
     * @param size    每页大小
     * @param keyword 搜索关键词
     * @return 分页结果
     */
    @Override
    public PageResult<User> getUserPage(Integer page, Integer size, String keyword) {
        log.debug("查询用户列表，页码: {}, 每页大小: {}, 关键词: {}", page, size, keyword);

        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword)
                    .or()
                    .like(User::getPhone, keyword));
        }

        wrapper.orderByDesc(User::getCreateTime);
        Page<User> userPage = this.page(pageParam, wrapper);

        log.debug("查询到 {} 条用户记录", userPage.getTotal());
        return new PageResult<>(userPage.getTotal(), userPage.getRecords(),
                userPage.getCurrent(), userPage.getSize());
    }

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @return 是否注册成功
     * @throws BusinessException 用户名已存在时抛出
     */
    @Override
    public boolean register(User user) {
        log.info("用户注册: {}", user.getUsername());

        User existUser = getByUsername(user.getUsername());
        if (existUser != null) {
            log.warn("注册失败，用户名已存在: {}", user.getUsername());
            throw new BusinessException("用户名已存在");
        }

        user.setPassword(PasswordUtil.encode(user.getPassword()));
        user.setRole(0);
        user.setStatus(1);

        boolean success = this.save(user);
        if (success) {
            log.info("用户注册成功: {}", user.getUsername());
        } else {
            log.error("用户注册失败: {}", user.getUsername());
        }
        return success;
    }

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 是否修改成功
     * @throws BusinessException 用户不存在或原密码错误时抛出
     */
    @Override
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        log.info("修改密码，用户ID: {}", userId);

        User user = this.getById(userId);
        if (user == null) {
            log.warn("修改密码失败，用户不存在: {}", userId);
            throw new BusinessException("用户不存在");
        }

        if (!PasswordUtil.matches(oldPassword, user.getPassword())) {
            log.warn("修改密码失败，原密码错误: {}", userId);
            throw new BusinessException("原密码错误");
        }

        user.setPassword(PasswordUtil.encode(newPassword));
        boolean result = this.updateById(user);

        if (result) {
            redisTemplate.delete(buildUserCacheKey(userId));
            log.info("密码修改成功，已清除用户缓存: {}", userId);
        } else {
            log.error("密码修改失败: {}", userId);
        }

        return result;
    }

    /**
     * 获取用户总数
     *
     * @return 用户总数
     */
    @Override
    public Long getUserCount() {
        String cacheKey = "stats:userCount";
        Long count = (Long) redisTemplate.opsForValue().get(cacheKey);
        if (count == null) {
            count = baseMapper.selectUserCount();
            redisTemplate.opsForValue().set(cacheKey, count, 5, TimeUnit.MINUTES);
        }
        return count;
    }

    /**
     * 构建用户缓存key
     *
     * @param userId 用户ID
     * @return 缓存key
     */
    private String buildUserCacheKey(Long userId) {
        return "user:" + userId;
    }
}
