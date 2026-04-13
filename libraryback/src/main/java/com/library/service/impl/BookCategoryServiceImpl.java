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

/**
 * 图书分类服务实现类
 * 提供分类的增删改查和树形结构构建功能
 */
@Slf4j
@Service
public class BookCategoryServiceImpl extends ServiceImpl<BookCategoryMapper, BookCategory> implements BookCategoryService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取所有分类列表
     * 使用Redis缓存提高查询效率
     */
    @Override
    public List<BookCategory> getAllCategories() {
        String cacheKey = "categories:all";
        List<BookCategory> categories = (List<BookCategory>) redisTemplate.opsForValue().get(cacheKey);
        if (categories == null) {
            log.debug("从数据库加载分类列表");
            categories = baseMapper.selectAllCategories();
            redisTemplate.opsForValue().set(cacheKey, categories, 10, TimeUnit.MINUTES);
        }
        return categories;
    }

    /**
     * 获取分类树形结构
     * 将扁平的分类列表转换为树形结构
     */
    @Override
    public List<BookCategory> getCategoryTree() {
        List<BookCategory> allCategories = getAllCategories();
        
        List<BookCategory> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .collect(Collectors.toList());
        
        for (BookCategory root : rootCategories) {
            buildCategoryTree(root, allCategories);
        }
        
        log.debug("构建分类树完成，根节点数量: {}", rootCategories.size());
        return rootCategories;
    }
    
    /**
     * 递归构建分类树
     * @param parent 父节点
     * @param allCategories 所有分类列表
     */
    private void buildCategoryTree(BookCategory parent, List<BookCategory> allCategories) {
        List<BookCategory> children = allCategories.stream()
                .filter(c -> parent.getId() != null && parent.getId().equals(c.getParentId()))
                .collect(Collectors.toList());
        
        parent.setChildren(children);
        
        for (BookCategory child : children) {
            buildCategoryTree(child, allCategories);
        }
    }

    /**
     * 根据分类ID获取分类名称
     * 使用Redis缓存提高查询效率
     */
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
