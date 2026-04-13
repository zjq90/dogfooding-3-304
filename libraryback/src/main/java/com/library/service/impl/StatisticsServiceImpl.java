package com.library.service.impl;

import com.library.service.BookInfoService;
import com.library.service.BorrowRecordService;
import com.library.service.StatisticsService;
import com.library.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private UserService userService;
    
    @Autowired
    private BookInfoService bookInfoService;
    
    @Autowired
    private BorrowRecordService borrowRecordService;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 用户统计
        stats.put("userCount", userService.getUserCount());
        
        // 图书统计
        stats.put("bookCount", bookInfoService.getBookCount());
        stats.put("totalBookQuantity", bookInfoService.getTotalBookQuantity());
        
        // 借阅统计
        stats.put("totalBorrowCount", borrowRecordService.getTotalBorrowCount());
        stats.put("overdueCount", borrowRecordService.getOverdueCount());
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getMonthlyBorrowTrend() {
        return borrowRecordService.getMonthlyBorrowStats();
    }

    @Override
    public List<Map<String, Object>> getHotCategoryStats() {
        return bookInfoService.getCategoryStats();
    }

    @Override
    public List<Map<String, Object>> getHotBookStats() {
        return borrowRecordService.getHotBooks();
    }

    @Override
    public Map<String, Object> getUserGrowthStats() {
        Map<String, Object> result = new HashMap<>();
        
        // 模拟用户增长数据（实际应该从数据库查询）
        List<String> months = new ArrayList<>();
        List<Integer> userCounts = new ArrayList<>();
        
        String[] monthNames = {"1月", "2月", "3月", "4月", "5月", "6月", 
                              "7月", "8月", "9月", "10月", "11月", "12月"};
        
        for (int i = 0; i < 12; i++) {
            months.add(monthNames[i]);
            userCounts.add(10 + i * 5 + (int)(Math.random() * 10));
        }
        
        result.put("months", months);
        result.put("userCounts", userCounts);
        
        return result;
    }
}
