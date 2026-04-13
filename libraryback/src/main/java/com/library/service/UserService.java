package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.User;

public interface UserService extends IService<User> {
    
    User login(String username, String password);
    
    User getByUsername(String username);
    
    PageResult<User> getUserPage(Integer page, Integer size, String keyword);
    
    boolean register(User user);
    
    boolean updatePassword(Long userId, String oldPassword, String newPassword);
    
    Long getUserCount();
}
