package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.BookCategory;

import java.util.List;

public interface BookCategoryService extends IService<BookCategory> {
    
    List<BookCategory> getAllCategories();
    
    List<BookCategory> getCategoryTree();
    
    String getCategoryName(Long categoryId);
}
