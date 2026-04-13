package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.BorrowRecord;
import com.library.service.BorrowRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/borrow")
public class BorrowRecordController {

    @Autowired
    private BorrowRecordService borrowRecordService;

    @GetMapping("/page")
    public Result<PageResult<BorrowRecord>> getBorrowPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) Integer status) {
        
        PageResult<BorrowRecord> result = borrowRecordService.getBorrowPage(page, size, userId, bookId, status);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<BorrowRecord> getBorrowById(@PathVariable Long id) {
        BorrowRecord record = borrowRecordService.getBorrowDetail(id);
        if (record != null) {
            return Result.success(record);
        }
        return Result.error("记录不存在");
    }

    @PostMapping
    public Result<Void> borrowBook(
            @RequestAttribute Long userId,
            @RequestParam Long bookId,
            @RequestParam(defaultValue = "30") Integer borrowDays) {
        
        log.info("用户 {} 借阅图书 {}", userId, bookId);
        
        boolean success = borrowRecordService.borrowBook(userId, bookId, borrowDays);
        if (success) {
            return Result.success("借阅成功");
        }
        return Result.error("借阅失败");
    }

    @PutMapping("/{id}/return")
    public Result<Void> returnBook(@PathVariable Long id) {
        log.info("归还图书，记录ID: {}", id);
        
        boolean success = borrowRecordService.returnBook(id);
        if (success) {
            return Result.success("归还成功");
        }
        return Result.error("归还失败");
    }

    @GetMapping("/my")
    public Result<PageResult<BorrowRecord>> getMyBorrows(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        
        PageResult<BorrowRecord> result = borrowRecordService.getBorrowPage(page, size, userId, null, status);
        return Result.success(result);
    }

    @GetMapping("/stats/monthly")
    public Result<List<Map<String, Object>>> getMonthlyStats() {
        List<Map<String, Object>> stats = borrowRecordService.getMonthlyBorrowStats();
        return Result.success(stats);
    }

    @GetMapping("/stats/hot")
    public Result<List<Map<String, Object>>> getHotBooks() {
        List<Map<String, Object>> hotBooks = borrowRecordService.getHotBooks();
        return Result.success(hotBooks);
    }

    @GetMapping("/count")
    public Result<Map<String, Object>> getBorrowCount() {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("totalBorrowCount", borrowRecordService.getTotalBorrowCount());
        result.put("overdueCount", borrowRecordService.getOverdueCount());
        return Result.success(result);
    }
}
