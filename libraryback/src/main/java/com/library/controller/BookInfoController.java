package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.BookInfo;
import com.library.service.BookInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/book")
public class BookInfoController {

    @Autowired
    private BookInfoService bookInfoService;

    @GetMapping("/page")
    public Result<PageResult<BookInfo>> getBookPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        log.info("查询图书列表，页码：{}，每页大小：{}，关键词：{}，分类ID：{}", page, size, keyword, categoryId);
        PageResult<BookInfo> result = bookInfoService.getBookPage(page, size, keyword, categoryId);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<BookInfo> getBookById(@PathVariable Long id) {
        log.info("查询图书详情，图书ID：{}", id);
        BookInfo book = bookInfoService.getBookDetail(id);
        if (book != null) {
            return Result.success(book);
        }
        log.warn("图书不存在，图书ID：{}", id);
        return Result.error("图书不存在");
    }

    @PostMapping
    public Result<Void> addBook(@RequestBody BookInfo book) {
        log.info("添加图书，书名：{}，ISBN：{}", book.getTitle(), book.getIsbn());
        boolean success = bookInfoService.save(book);
        if (success) {
            log.info("图书添加成功，书名：{}", book.getTitle());
            return Result.success("添加成功");
        }
        log.error("图书添加失败，书名：{}", book.getTitle());
        return Result.error("添加失败");
    }

    @PutMapping("/{id}")
    public Result<Void> updateBook(@PathVariable Long id, @RequestBody BookInfo book) {
        log.info("更新图书信息，图书ID：{}", id);
        book.setId(id);
        boolean success = bookInfoService.updateById(book);
        if (success) {
            log.info("图书信息更新成功，图书ID：{}", id);
            return Result.success("更新成功");
        }
        log.error("图书信息更新失败，图书ID：{}", id);
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBook(@PathVariable Long id) {
        log.info("删除图书，图书ID：{}", id);
        boolean success = bookInfoService.removeById(id);
        if (success) {
            log.info("图书删除成功，图书ID：{}", id);
            return Result.success("删除成功");
        }
        log.error("图书删除失败，图书ID：{}", id);
        return Result.error("删除失败");
    }

    @GetMapping("/stats/category")
    public Result<List<Map<String, Object>>> getCategoryStats() {
        log.info("获取图书分类统计");
        List<Map<String, Object>> stats = bookInfoService.getCategoryStats();
        return Result.success(stats);
    }

    @GetMapping("/count")
    public Result<Map<String, Long>> getBookCount() {
        log.info("获取图书数量统计");
        Map<String, Long> result = new java.util.HashMap<>();
        result.put("bookCount", bookInfoService.getBookCount());
        result.put("totalQuantity", bookInfoService.getTotalBookQuantity());
        return Result.success(result);
    }
}
