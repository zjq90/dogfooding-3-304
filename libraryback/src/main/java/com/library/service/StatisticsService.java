package com.library.service;

import java.util.List;
import java.util.Map;

/**
 * 统计服务接口
 * 定义统计相关的业务方法
 */
public interface StatisticsService {
    
    Map<String, Object> getDashboardStats();
    
    List<Map<String, Object>> getMonthlyBorrowTrend();
    
    List<Map<String, Object>> getHotCategoryStats();
    
    List<Map<String, Object>> getHotBookStats();
    
    Map<String, Object> getUserGrowthStats();
}
