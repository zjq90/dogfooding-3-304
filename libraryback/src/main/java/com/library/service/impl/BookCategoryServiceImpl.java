package com.library.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.BookCategory;
import com.library.mapper.BookCategoryMapper;
import com.library.service.BookCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 图书分类服务实现类
 */
@Slf4j
@Service
public class BookCategoryServiceImpl extends ServiceImpl<BookCategoryMapper, BookCategory> implements BookCategoryService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取所有分类
     *
     * @return 分类列表
     */
    @Override
    public List<BookCategory> getAllCategories() {
        String cacheKey = "categories:all";
        List<BookCategory> categories = (List<BookCategory>) redisTemplate.opsForValue().get(cacheKey);

        if (categories == null) {
            log.debug("缓存未命中，从数据库查询所有分类");
            categories = baseMapper.selectAllCategories();
            redisTemplate.opsForValue().set(cacheKey, categories, 10, TimeUnit.MINUTES);
        }
        return categories;
    }

    /**
     * 获取分类树
     *
     * @return 分类树列表
     */
    @Override
    public List<BookCategory> getCategoryTree() {
        List<BookCategory> allCategories = getAllCategories();

        List<BookCategory> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == 0)
                .collect(Collectors.toList());

        for (BookCategory root : rootCategories) {
            buildCategoryTree(root, allCategories);
        }

        return rootCategories;
    }

    /**
     * 构建分类树
     *
     * @param parent        父分类
     * @param allCategories 所有分类
     */
    private void buildCategoryTree(BookCategory parent, List<BookCategory> allCategories) {
        List<BookCategory> children = allCategories.stream()
                .filter(c -> c.getParentId().equals(parent.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 获取分类名称
     *
     * @param categoryId 分类ID
     * @return 分类名称
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
