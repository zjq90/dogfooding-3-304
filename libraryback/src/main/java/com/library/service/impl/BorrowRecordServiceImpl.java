package com.library.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.BusinessException;
import com.library.common.PageResult;
import com.library.entity.BorrowRecord;
import com.library.mapper.BorrowRecordMapper;
import com.library.service.BookInfoService;
import com.library.service.BorrowRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 借阅记录服务实现类
 */
@Slf4j
@Service
public class BorrowRecordServiceImpl extends ServiceImpl<BorrowRecordMapper, BorrowRecord> implements BorrowRecordService {

    @Autowired
    private BookInfoService bookInfoService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 分页查询借阅记录
     *
     * @param page   页码
     * @param size   每页大小
     * @param userId 用户ID
     * @param bookId 图书ID
     * @param status 状态
     * @return 分页结果
     */
    @Override
    public PageResult<BorrowRecord> getBorrowPage(Integer page, Integer size, Long userId, Long bookId, Integer status) {
        log.debug("查询借阅记录，页码: {}, 每页大小: {}, 用户ID: {}, 图书ID: {}, 状态: {}",
                page, size, userId, bookId, status);

        Page<BorrowRecord> pageParam = new Page<>(page, size);
        Page<BorrowRecord> recordPage = baseMapper.selectBorrowPage(pageParam, userId, bookId, status);

        log.debug("查询到 {} 条借阅记录", recordPage.getTotal());
        return new PageResult<>(recordPage.getTotal(), recordPage.getRecords(),
                recordPage.getCurrent(), recordPage.getSize());
    }

    /**
     * 获取借阅详情
     *
     * @param id 记录ID
     * @return 借阅详情
     */
    @Override
    public BorrowRecord getBorrowDetail(Long id) {
        return baseMapper.selectBorrowDetail(id);
    }

    /**
     * 借阅图书
     *
     * @param userId     用户ID
     * @param bookId     图书ID
     * @param borrowDays 借阅天数
     * @return 是否借阅成功
     * @throws BusinessException 达到借阅上限或库存不足时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean borrowBook(Long userId, Long bookId, Integer borrowDays) {
        log.info("用户借阅图书，用户ID: {}, 图书ID: {}, 借阅天数: {}", userId, bookId, borrowDays);

        Integer borrowingCount = getBorrowingCount(userId);
        if (borrowingCount >= 5) {
            log.warn("借阅失败，用户 {} 已达到最大借阅数量限制", userId);
            throw new BusinessException("您已达到最大借阅数量限制(5本)");
        }

        boolean borrowSuccess = bookInfoService.borrowBook(bookId, userId);
        if (!borrowSuccess) {
            log.warn("借阅失败，图书 {} 库存不足", bookId);
            throw new BusinessException("借阅失败，图书库存不足");
        }

        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(borrowDays));
        record.setStatus(0);

        boolean saveSuccess = this.save(record);
        if (saveSuccess) {
            clearStatsCache();
            log.info("借阅成功，用户ID: {}, 图书ID: {}", userId, bookId);
        } else {
            log.error("借阅记录保存失败，用户ID: {}, 图书ID: {}", userId, bookId);
        }

        return saveSuccess;
    }

    /**
     * 归还图书
     *
     * @param recordId 借阅记录ID
     * @return 是否归还成功
     * @throws BusinessException 记录不存在或已归还时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnBook(Long recordId) {
        log.info("归还图书，记录ID: {}", recordId);

        BorrowRecord record = this.getById(recordId);
        if (record == null) {
            log.warn("归还失败，借阅记录不存在: {}", recordId);
            throw new BusinessException("借阅记录不存在");
        }
        if (record.getStatus() == 1) {
            log.warn("归还失败，图书已归还: {}", recordId);
            throw new BusinessException("该图书已归还");
        }

        boolean returnSuccess = bookInfoService.returnBook(record.getBookId(), record.getUserId());
        if (!returnSuccess) {
            log.error("归还失败，图书服务返回失败: {}", recordId);
            throw new BusinessException("归还失败");
        }

        record.setReturnDate(LocalDate.now());
        record.setStatus(1);

        if (LocalDate.now().isAfter(record.getDueDate())) {
            record.setStatus(2);
            log.info("图书逾期归还，记录ID: {}", recordId);
        }

        boolean updateSuccess = this.updateById(record);
        if (updateSuccess) {
            clearStatsCache();
            log.info("归还成功，记录ID: {}", recordId);
        } else {
            log.error("归还失败，更新记录失败: {}", recordId);
        }

        return updateSuccess;
    }

    /**
     * 获取用户当前借阅数量
     *
     * @param userId 用户ID
     * @return 借阅数量
     */
    @Override
    public Integer getBorrowingCount(Long userId) {
        return baseMapper.selectBorrowingCount(userId);
    }

    /**
     * 获取逾期数量
     *
     * @return 逾期数量
     */
    @Override
    public Integer getOverdueCount() {
        return baseMapper.selectOverdueCount(LocalDate.now());
    }

    /**
     * 获取月度借阅统计
     *
     * @return 月度统计列表
     */
    @Override
    public List<Map<String, Object>> getMonthlyBorrowStats() {
        String cacheKey = "stats:monthlyBorrow";
        List<Map<String, Object>> stats = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);
        if (stats == null) {
            stats = baseMapper.selectMonthlyBorrowStats();
            redisTemplate.opsForValue().set(cacheKey, stats, 10, TimeUnit.MINUTES);
        }
        return stats;
    }

    /**
     * 获取热门图书
     *
     * @return 热门图书列表
     */
    @Override
    public List<Map<String, Object>> getHotBooks() {
        String cacheKey = "stats:hotBooks";
        List<Map<String, Object>> hotBooks = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);
        if (hotBooks == null) {
            hotBooks = baseMapper.selectHotBooks();
            redisTemplate.opsForValue().set(cacheKey, hotBooks, 10, TimeUnit.MINUTES);
        }
        return hotBooks;
    }

    /**
     * 获取总借阅数量
     *
     * @return 总借阅数量
     */
    @Override
    public Long getTotalBorrowCount() {
        String cacheKey = "stats:totalBorrowCount";
        Long count = (Long) redisTemplate.opsForValue().get(cacheKey);
        if (count == null) {
            count = baseMapper.selectTotalBorrowCount();
            redisTemplate.opsForValue().set(cacheKey, count, 5, TimeUnit.MINUTES);
        }
        return count;
    }

    /**
     * 清除统计缓存
     */
    private void clearStatsCache() {
        redisTemplate.delete("stats:monthlyBorrow");
        redisTemplate.delete("stats:hotBooks");
        redisTemplate.delete("stats:totalBorrowCount");
        redisTemplate.delete("stats:overdueCount");
    }
}
