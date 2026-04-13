package com.library.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.BusinessException;
import com.library.common.PageResult;
import com.library.entity.BookInfo;
import com.library.mapper.BookInfoMapper;
import com.library.service.BookInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 图书信息服务实现类
 * 提供图书的增删改查、借阅、归还等功能
 */
@Slf4j
@Service
public class BookInfoServiceImpl extends ServiceImpl<BookInfoMapper, BookInfo> implements BookInfoService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public PageResult<BookInfo> getBookPage(Integer page, Integer size, String keyword, Long categoryId) {
        Page<BookInfo> pageParam = new Page<>(page, size);
        Page<BookInfo> bookPage = baseMapper.selectBookPage(pageParam, keyword, categoryId);
        return new PageResult<>(bookPage.getTotal(), bookPage.getRecords(),
                                bookPage.getCurrent(), bookPage.getSize());
    }

    @Override
    public BookInfo getBookDetail(Long id) {
        String cacheKey = "book:" + id;
        BookInfo book = (BookInfo) redisTemplate.opsForValue().get(cacheKey);
        if (book == null) {
            book = baseMapper.selectBookDetail(id);
            if (book != null) {
                redisTemplate.opsForValue().set(cacheKey, book, 10, TimeUnit.MINUTES);
            }
        }
        return book;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean borrowBook(Long bookId, Long userId) {
        BookInfo book = this.getById(bookId);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        if (book.getAvailableQuantity() <= 0) {
            throw new BusinessException("图书已借完");
        }
        
        int result = baseMapper.updateAvailableQuantity(bookId, -1);
        if (result > 0) {
            // 清除缓存
            redisTemplate.delete("book:" + bookId);
            redisTemplate.delete("stats:bookCount");
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnBook(Long bookId, Long userId) {
        BookInfo book = this.getById(bookId);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        
        int result = baseMapper.updateAvailableQuantity(bookId, 1);
        if (result > 0) {
            // 清除缓存
            redisTemplate.delete("book:" + bookId);
            redisTemplate.delete("stats:bookCount");
            return true;
        }
        return false;
    }

    @Override
    public Long getBookCount() {
        String cacheKey = "stats:bookCount";
        Long count = (Long) redisTemplate.opsForValue().get(cacheKey);
        if (count == null) {
            count = baseMapper.selectBookCount();
            redisTemplate.opsForValue().set(cacheKey, count, 5, TimeUnit.MINUTES);
        }
        return count;
    }

    @Override
    public Long getTotalBookQuantity() {
        String cacheKey = "stats:totalBookQuantity";
        Long count = (Long) redisTemplate.opsForValue().get(cacheKey);
        if (count == null) {
            count = baseMapper.selectTotalBookQuantity();
            redisTemplate.opsForValue().set(cacheKey, count, 5, TimeUnit.MINUTES);
        }
        return count;
    }

    @Override
    public List<Map<String, Object>> getCategoryStats() {
        return baseMapper.selectCategoryStats();
    }
}
