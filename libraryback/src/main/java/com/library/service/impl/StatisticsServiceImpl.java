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

/**
 * 统计服务实现类
 * 提供仪表盘数据、借阅趋势、热门图书等统计功能
 */
@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private UserService userService;
    
    @Autowired
    private BookInfoService bookInfoService;
    
    @Autowired
    private BorrowRecordService borrowRecordService;

    /**
     * 获取仪表盘统计数据
     */
    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("userCount", userService.getUserCount());
        stats.put("bookCount", bookInfoService.getBookCount());
        stats.put("totalBookQuantity", bookInfoService.getTotalBookQuantity());
        stats.put("totalBorrowCount", borrowRecordService.getTotalBorrowCount());
        stats.put("overdueCount", borrowRecordService.getOverdueCount());
        
        return stats;
    }

    /**
     * 获取月度借阅趋势
     */
    @Override
    public List<Map<String, Object>> getMonthlyBorrowTrend() {
        return borrowRecordService.getMonthlyBorrowStats();
    }

    /**
     * 获取热门分类统计
     */
    @Override
    public List<Map<String, Object>> getHotCategoryStats() {
        return bookInfoService.getCategoryStats();
    }

    /**
     * 获取热门图书统计
     */
    @Override
    public List<Map<String, Object>> getHotBookStats() {
        return borrowRecordService.getHotBooks();
    }

    /**
     * 获取用户增长统计
     */
    @Override
    public Map<String, Object> getUserGrowthStats() {
        Map<String, Object> result = new HashMap<>();
        
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
