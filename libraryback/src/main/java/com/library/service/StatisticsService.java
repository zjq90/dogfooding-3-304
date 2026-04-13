package com.library.service;

import java.util.List;
import java.util.Map;

public interface StatisticsService {
    
    Map<String, Object> getDashboardStats();
    
    List<Map<String, Object>> getMonthlyBorrowTrend();
    
    List<Map<String, Object>> getHotCategoryStats();
    
    List<Map<String, Object>> getHotBookStats();
    
    Map<String, Object> getUserGrowthStats();
}
