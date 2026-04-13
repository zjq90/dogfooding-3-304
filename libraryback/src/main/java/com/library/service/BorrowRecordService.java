package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.common.PageResult;
import com.library.entity.BorrowRecord;

import java.util.List;
import java.util.Map;

public interface BorrowRecordService extends IService<BorrowRecord> {
    
    PageResult<BorrowRecord> getBorrowPage(Integer page, Integer size, Long userId, Long bookId, Integer status);
    
    BorrowRecord getBorrowDetail(Long id);
    
    boolean borrowBook(Long userId, Long bookId, Integer borrowDays);
    
    boolean returnBook(Long recordId);
    
    Integer getBorrowingCount(Long userId);
    
    Integer getOverdueCount();
    
    List<Map<String, Object>> getMonthlyBorrowStats();
    
    List<Map<String, Object>> getHotBooks();
    
    Long getTotalBorrowCount();
}
