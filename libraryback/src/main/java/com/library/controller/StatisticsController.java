package com.library.controller;

import com.library.common.Result;
import com.library.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 统计控制器
 * 提供仪表盘数据、借阅趋势、热门图书等统计功能
 */
@Slf4j
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = statisticsService.getDashboardStats();
        return Result.success(stats);
    }

    /**
     * 获取月度借阅趋势
     */
    @GetMapping("/monthly-trend")
    public Result<List<Map<String, Object>>> getMonthlyTrend() {
        List<Map<String, Object>> trend = statisticsService.getMonthlyBorrowTrend();
        return Result.success(trend);
    }

    /**
     * 获取热门分类统计
     */
    @GetMapping("/hot-categories")
    public Result<List<Map<String, Object>>> getHotCategories() {
        List<Map<String, Object>> categories = statisticsService.getHotCategoryStats();
        return Result.success(categories);
    }

    /**
     * 获取热门图书统计
     */
    @GetMapping("/hot-books")
    public Result<List<Map<String, Object>>> getHotBooks() {
        List<Map<String, Object>> books = statisticsService.getHotBookStats();
        return Result.success(books);
    }

    /**
     * 获取用户增长统计
     */
    @GetMapping("/user-growth")
    public Result<Map<String, Object>> getUserGrowth() {
        Map<String, Object> growth = statisticsService.getUserGrowthStats();
        return Result.success(growth);
    }
}
