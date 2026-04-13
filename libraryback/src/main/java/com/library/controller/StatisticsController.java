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

@Slf4j
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = statisticsService.getDashboardStats();
        return Result.success(stats);
    }

    @GetMapping("/monthly-trend")
    public Result<List<Map<String, Object>>> getMonthlyTrend() {
        List<Map<String, Object>> trend = statisticsService.getMonthlyBorrowTrend();
        return Result.success(trend);
    }

    @GetMapping("/hot-categories")
    public Result<List<Map<String, Object>>> getHotCategories() {
        List<Map<String, Object>> categories = statisticsService.getHotCategoryStats();
        return Result.success(categories);
    }

    @GetMapping("/hot-books")
    public Result<List<Map<String, Object>>> getHotBooks() {
        List<Map<String, Object>> books = statisticsService.getHotBookStats();
        return Result.success(books);
    }

    @GetMapping("/user-growth")
    public Result<Map<String, Object>> getUserGrowth() {
        Map<String, Object> growth = statisticsService.getUserGrowthStats();
        return Result.success(growth);
    }
}
