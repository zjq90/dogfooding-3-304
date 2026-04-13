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

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public User login(String username, String password) {
        User user = baseMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        if (!PasswordUtil.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 缓存用户信息
        String cacheKey = "user:" + user.getId();
        redisTemplate.opsForValue().set(cacheKey, user, 30, TimeUnit.MINUTES);
        
        return user;
    }

    @Override
    public User getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    public PageResult<User> getUserPage(Integer page, Integer size, String keyword) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword)
                    .or()
                    .like(User::getPhone, keyword));
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> userPage = this.page(pageParam, wrapper);
        
        return new PageResult<>(userPage.getTotal(), userPage.getRecords(),
                userPage.getCurrent(), userPage.getSize());
    }

    @Override
    public boolean register(User user) {
        log.info("用户注册，用户名: {}", user.getUsername());
        // 检查用户名是否已存在
        User existUser = getByUsername(user.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 加密密码
        user.setPassword(PasswordUtil.encode(user.getPassword()));
        user.setRole(0); // 普通用户
        user.setStatus(1); // 启用
        
        boolean result = this.save(user);
        log.info("用户注册成功，用户名: {}", user.getUsername());
        return result;
    }

    @Override
    public boolean addUser(User user) {
        log.info("管理员添加用户，用户名: {}", user.getUsername());
        // 检查用户名是否已存在
        User existUser = getByUsername(user.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 加密密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(PasswordUtil.encode(user.getPassword()));
        } else {
            // 默认密码
            user.setPassword(PasswordUtil.encode("123456"));
        }
        
        if (user.getStatus() == null) {
            user.setStatus(1); // 默认启用
        }
        
        boolean result = this.save(user);
        if (result) {
            log.info("用户添加成功，用户名: {}", user.getUsername());
        } else {
            log.error("用户添加失败，用户名: {}", user.getUsername());
        }
        return result;
    }

    @Override
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (!PasswordUtil.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        user.setPassword(PasswordUtil.encode(newPassword));
        boolean result = this.updateById(user);
        
        // 清除缓存
        if (result) {
            redisTemplate.delete("user:" + userId);
        }
        
        return result;
    }

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
}
