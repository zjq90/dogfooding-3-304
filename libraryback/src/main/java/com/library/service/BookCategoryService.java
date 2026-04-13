package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.BookCategory;

import java.util.List;

/**
 * 图书分类服务接口
 * 定义分类相关的业务方法
 */
public interface BookCategoryService extends IService<BookCategory> {
    
    List<BookCategory> getAllCategories();
    
    List<BookCategory> getCategoryTree();
    
    String getCategoryName(Long categoryId);
}
