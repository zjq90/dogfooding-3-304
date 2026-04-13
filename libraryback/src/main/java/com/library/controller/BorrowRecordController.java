package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.BorrowRecord;
import com.library.service.BorrowRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 借阅记录控制器
 * 处理图书借阅、归还、借阅记录查询和统计功能
 */
@Slf4j
@RestController
@RequestMapping("/borrow")
public class BorrowRecordController {

    @Autowired
    private BorrowRecordService borrowRecordService;

    /**
     * 分页查询借阅记录
     * @param page 页码
     * @param size 每页大小
     * @param userId 用户ID（可选）
     * @param bookId 图书ID（可选）
     * @param status 借阅状态（可选）0:借阅中 1:已归还 2:逾期归还
     * @return 分页后的借阅记录列表
     */
    @GetMapping("/page")
    public Result<PageResult<BorrowRecord>> getBorrowPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) Integer status) {
        
        log.info("查询借阅记录列表，页码：{}，每页大小：{}，用户ID：{}，图书ID：{}，状态：{}", page, size, userId, bookId, status);
        PageResult<BorrowRecord> result = borrowRecordService.getBorrowPage(page, size, userId, bookId, status);
        return Result.success(result);
    }

    /**
     * 根据ID查询借阅详情
     * @param id 借阅记录ID
     * @return 借阅详情信息
     */
    @GetMapping("/{id}")
    public Result<BorrowRecord> getBorrowById(@PathVariable Long id) {
        log.info("查询借阅详情，记录ID：{}", id);
        BorrowRecord record = borrowRecordService.getBorrowDetail(id);
        if (record != null) {
            return Result.success(record);
        }
        log.warn("借阅记录不存在，记录ID：{}", id);
        return Result.error("记录不存在");
    }

    /**
     * 借阅图书
     * @param userId 当前登录用户ID（从JWT拦截器注入）
     * @param bookId 图书ID
     * @param borrowDays 借阅天数，默认30天
     * @return 借阅结果
     */
    @PostMapping
    public Result<Void> borrowBook(
            @RequestAttribute(name = "userId") Long userId,
            @RequestParam Long bookId,
            @RequestParam(defaultValue = "30") Integer borrowDays) {
        
        log.info("用户 {} 借阅图书 {}", userId, bookId);
        
        boolean success = borrowRecordService.borrowBook(userId, bookId, borrowDays);
        if (success) {
            log.info("借阅成功，用户ID：{}，图书ID：{}", userId, bookId);
            return Result.success("借阅成功");
        }
        log.error("借阅失败，用户ID：{}，图书ID：{}", userId, bookId);
        return Result.error("借阅失败");
    }

    /**
     * 归还图书
     * @param id 借阅记录ID
     * @return 归还结果
     */
    @PutMapping("/{id}/return")
    public Result<Void> returnBook(@PathVariable Long id) {
        log.info("归还图书，记录ID: {}", id);
        
        boolean success = borrowRecordService.returnBook(id);
        if (success) {
            log.info("归还成功，记录ID：{}", id);
            return Result.success("归还成功");
        }
        log.error("归还失败，记录ID：{}", id);
        return Result.error("归还失败");
    }

    /**
     * 查询当前用户的借阅记录
     * @param userId 当前登录用户ID（从JWT拦截器注入）
     * @param page 页码
     * @param size 每页大小
     * @param status 借阅状态（可选）
     * @return 分页后的当前用户借阅记录
     */
    @GetMapping("/my")
    public Result<PageResult<BorrowRecord>> getMyBorrows(
            @RequestAttribute(name = "userId") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        
        log.info("查询用户 {} 的借阅记录，页码：{}，状态：{}", userId, page, status);
        PageResult<BorrowRecord> result = borrowRecordService.getBorrowPage(page, size, userId, null, status);
        return Result.success(result);
    }

    /**
     * 获取月度借阅统计数据
     * @return 近12个月的借阅统计
     */
    @GetMapping("/stats/monthly")
    public Result<List<Map<String, Object>>> getMonthlyStats() {
        log.info("获取月度借阅统计");
        List<Map<String, Object>> stats = borrowRecordService.getMonthlyBorrowStats();
        return Result.success(stats);
    }

    /**
     * 获取热门图书排行
     * @return 借阅量最高的前10本图书
     */
    @GetMapping("/stats/hot")
    public Result<List<Map<String, Object>>> getHotBooks() {
        log.info("获取热门图书排行");
        List<Map<String, Object>> hotBooks = borrowRecordService.getHotBooks();
        return Result.success(hotBooks);
    }

    /**
     * 获取借阅数量统计
     * @return 总借阅数量和逾期数量
     */
    @GetMapping("/count")
    public Result<Map<String, Object>> getBorrowCount() {
        log.info("获取借阅数量统计");
        Map<String, Object> result = new HashMap<>();
        result.put("totalBorrowCount", borrowRecordService.getTotalBorrowCount());
        result.put("overdueCount", borrowRecordService.getOverdueCount());
        return Result.success(result);
    }
}
