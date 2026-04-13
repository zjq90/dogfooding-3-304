package com.library.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.BookCategory;
import com.library.mapper.BookCategoryMapper;
import com.library.service.BookCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookCategoryServiceImpl extends ServiceImpl<BookCategoryMapper, BookCategory> implements BookCategoryService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<BookCategory> getAllCategories() {
        String cacheKey = "categories:all";
        List<BookCategory> categories = (List<BookCategory>) redisTemplate.opsForValue().get(cacheKey);
        if (categories == null) {
            categories = baseMapper.selectAllCategories();
            redisTemplate.opsForValue().set(cacheKey, categories, 10, TimeUnit.MINUTES);
        }
        return categories;
    }

    @Override
    public List<BookCategory> getCategoryTree() {
        List<BookCategory> allCategories = getAllCategories();
        
        // 构建树形结构
        List<BookCategory> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == 0)
                .collect(Collectors.toList());
        
        for (BookCategory root : rootCategories) {
            buildCategoryTree(root, allCategories);
        }
        
        return rootCategories;
    }
    
    private void buildCategoryTree(BookCategory parent, List<BookCategory> allCategories) {
        List<BookCategory> children = allCategories.stream()
                .filter(c -> c.getParentId().equals(parent.getId()))
                .collect(Collectors.toList());
        
        // 这里可以扩展为递归构建多级树
    }

    @Override
    public String getCategoryName(Long categoryId) {
        if (categoryId == null) {
            return "";
        }
        String cacheKey = "category:name:" + categoryId;
        String name = (String) redisTemplate.opsForValue().get(cacheKey);
        if (name == null) {
            name = baseMapper.selectNameById(categoryId);
            if (name != null) {
                redisTemplate.opsForValue().set(cacheKey, name, 30, TimeUnit.MINUTES);
            }
        }
        return name;
    }
}
