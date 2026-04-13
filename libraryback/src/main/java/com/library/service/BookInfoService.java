package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.BookInfo;

import java.util.List;
import java.util.Map;

public interface BookInfoService extends IService<BookInfo> {
    
    PageResult<BookInfo> getBookPage(Integer page, Integer size, String keyword, Long categoryId);
    
    BookInfo getBookDetail(Long id);
    
    boolean borrowBook(Long bookId, Long userId);
    
    boolean returnBook(Long bookId, Long userId);
    
    Long getBookCount();
    
    Long getTotalBookQuantity();
    
    List<Map<String, Object>> getCategoryStats();
}
