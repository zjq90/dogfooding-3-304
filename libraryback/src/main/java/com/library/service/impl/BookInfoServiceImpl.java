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
 */
@Slf4j
@Service
public class BookInfoServiceImpl extends ServiceImpl<BookInfoMapper, BookInfo> implements BookInfoService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 分页查询图书列表
     *
     * @param page       页码
     * @param size       每页大小
     * @param keyword    搜索关键词
     * @param categoryId 分类ID
     * @return 分页结果
     */
    @Override
    public PageResult<BookInfo> getBookPage(Integer page, Integer size, String keyword, Long categoryId) {
        log.debug("查询图书列表，页码: {}, 每页大小: {}, 关键词: {}, 分类ID: {}", page, size, keyword, categoryId);

        Page<BookInfo> pageParam = new Page<>(page, size);
        Page<BookInfo> bookPage = baseMapper.selectBookPage(pageParam, keyword, categoryId);

        log.debug("查询到 {} 条图书记录", bookPage.getTotal());
        return new PageResult<>(bookPage.getTotal(), bookPage.getRecords(),
                bookPage.getCurrent(), bookPage.getSize());
    }

    /**
     * 获取图书详情
     *
     * @param id 图书ID
     * @return 图书详情
     */
    @Override
    public BookInfo getBookDetail(Long id) {
        String cacheKey = buildBookCacheKey(id);
        BookInfo book = (BookInfo) redisTemplate.opsForValue().get(cacheKey);

        if (book == null) {
            log.debug("缓存未命中，从数据库查询图书详情: {}", id);
            book = baseMapper.selectBookDetail(id);
            if (book != null) {
                redisTemplate.opsForValue().set(cacheKey, book, 10, TimeUnit.MINUTES);
            }
        }
        return book;
    }

    /**
     * 借阅图书
     *
     * @param bookId 图书ID
     * @param userId 用户ID
     * @return 是否借阅成功
     * @throws BusinessException 图书不存在或库存不足时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean borrowBook(Long bookId, Long userId) {
        log.info("借阅图书，图书ID: {}, 用户ID: {}", bookId, userId);

        BookInfo book = this.getById(bookId);
        if (book == null) {
            log.warn("借阅失败，图书不存在: {}", bookId);
            throw new BusinessException("图书不存在");
        }
        if (book.getAvailableQuantity() <= 0) {
            log.warn("借阅失败，图书库存不足: {}", bookId);
            throw new BusinessException("图书已借完");
        }

        int result = baseMapper.updateAvailableQuantity(bookId, -1);
        if (result > 0) {
            clearBookCache(bookId);
            log.info("借阅成功，图书ID: {}", bookId);
            return true;
        }
        log.error("借阅失败，更新库存失败，图书ID: {}", bookId);
        return false;
    }

    /**
     * 归还图书
     *
     * @param bookId 图书ID
     * @param userId 用户ID
     * @return 是否归还成功
     * @throws BusinessException 图书不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnBook(Long bookId, Long userId) {
        log.info("归还图书，图书ID: {}, 用户ID: {}", bookId, userId);

        BookInfo book = this.getById(bookId);
        if (book == null) {
            log.warn("归还失败，图书不存在: {}", bookId);
            throw new BusinessException("图书不存在");
        }

        int result = baseMapper.updateAvailableQuantity(bookId, 1);
        if (result > 0) {
            clearBookCache(bookId);
            log.info("归还成功，图书ID: {}", bookId);
            return true;
        }
        log.error("归还失败，更新库存失败，图书ID: {}", bookId);
        return false;
    }

    /**
     * 获取图书总数
     *
     * @return 图书总数
     */
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

    /**
     * 获取图书总库存
     *
     * @return 图书总库存
     */
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

    /**
     * 获取分类统计
     *
     * @return 分类统计列表
     */
    @Override
    public List<Map<String, Object>> getCategoryStats() {
        return baseMapper.selectCategoryStats();
    }

    /**
     * 构建图书缓存key
     *
     * @param bookId 图书ID
     * @return 缓存key
     */
    private String buildBookCacheKey(Long bookId) {
        return "book:" + bookId;
    }

    /**
     * 清除图书相关缓存
     *
     * @param bookId 图书ID
     */
    private void clearBookCache(Long bookId) {
        redisTemplate.delete(buildBookCacheKey(bookId));
        redisTemplate.delete("stats:bookCount");
        redisTemplate.delete("stats:totalBookQuantity");
    }
}
