package com.library.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.BusinessException;
import com.library.common.PageResult;
import com.library.entity.BookInfo;
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
 * 提供图书借阅、归还、统计等功能
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
     */
    @Override
    public PageResult<BorrowRecord> getBorrowPage(Integer page, Integer size, Long userId, Long bookId, Integer status) {
        Page<BorrowRecord> pageParam = new Page<>(page, size);
        Page<BorrowRecord> recordPage = baseMapper.selectBorrowPage(pageParam, userId, bookId, status);
        return new PageResult<>(recordPage.getTotal(), recordPage.getRecords(),
                                recordPage.getCurrent(), recordPage.getSize());
    }

    /**
     * 获取借阅记录详情
     */
    @Override
    public BorrowRecord getBorrowDetail(Long id) {
        return baseMapper.selectBorrowDetail(id);
    }

    /**
     * 借阅图书
     * 检查用户借阅数量限制和逾期情况
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean borrowBook(Long userId, Long bookId, Integer borrowDays) {
        Integer borrowingCount = getBorrowingCount(userId);
        if (borrowingCount >= 5) {
            throw new BusinessException("您已达到最大借阅数量限制(5本)");
        }
        
        Integer overdueCount = baseMapper.selectUserOverdueCount(userId, LocalDate.now());
        if (overdueCount > 0) {
            throw new BusinessException("您有逾期未还的图书，请先归还后再借阅");
        }
        
        boolean borrowSuccess = bookInfoService.borrowBook(bookId, userId);
        if (!borrowSuccess) {
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
            log.info("用户 {} 成功借阅图书 {}, 借阅天数: {}天", userId, bookId, borrowDays);
        }
        
        return saveSuccess;
    }

    /**
     * 归还图书
     * 自动检测是否逾期并更新状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnBook(Long recordId) {
        BorrowRecord record = this.getById(recordId);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }
        if (record.getStatus() == 1) {
            throw new BusinessException("该图书已归还");
        }
        
        boolean returnSuccess = bookInfoService.returnBook(record.getBookId(), record.getUserId());
        if (!returnSuccess) {
            throw new BusinessException("归还失败");
        }
        
        record.setReturnDate(LocalDate.now());
        record.setStatus(1);
        
        if (LocalDate.now().isAfter(record.getDueDate())) {
            record.setStatus(2);
            log.warn("图书归还逾期，记录ID: {}, 应还日期: {}", recordId, record.getDueDate());
        }
        
        boolean updateSuccess = this.updateById(record);
        if (updateSuccess) {
            clearStatsCache();
            log.info("图书归还成功，记录ID: {}", recordId);
        }
        
        return updateSuccess;
    }

    /**
     * 获取用户当前借阅中的数量
     */
    @Override
    public Integer getBorrowingCount(Long userId) {
        return baseMapper.selectBorrowingCount(userId);
    }

    /**
     * 获取逾期未还数量
     */
    @Override
    public Integer getOverdueCount() {
        return baseMapper.selectOverdueCount(LocalDate.now());
    }

    /**
     * 获取月度借阅统计
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
     * 获取热门图书列表
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
     * 获取总借阅次数
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
    }
}
