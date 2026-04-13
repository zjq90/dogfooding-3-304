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

@Slf4j
@Service
public class BorrowRecordServiceImpl extends ServiceImpl<BorrowRecordMapper, BorrowRecord> implements BorrowRecordService {

    @Autowired
    private BookInfoService bookInfoService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public PageResult<BorrowRecord> getBorrowPage(Integer page, Integer size, Long userId, Long bookId, Integer status) {
        Page<BorrowRecord> pageParam = new Page<>(page, size);
        Page<BorrowRecord> recordPage = baseMapper.selectBorrowPage(pageParam, userId, bookId, status);
        return new PageResult<>(recordPage.getTotal(), recordPage.getRecords(),
                recordPage.getCurrent(), recordPage.getSize());
    }

    @Override
    public BorrowRecord getBorrowDetail(Long id) {
        return baseMapper.selectBorrowDetail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean borrowBook(Long userId, Long bookId, Integer borrowDays) {
        // 检查用户当前借阅数量
        Integer borrowingCount = getBorrowingCount(userId);
        if (borrowingCount >= 5) {
            throw new BusinessException("您已达到最大借阅数量限制(5本)");
        }
        
        // 检查是否有逾期未还
        // TODO: 实现逾期检查
        
        // 借阅图书
        boolean borrowSuccess = bookInfoService.borrowBook(bookId, userId);
        if (!borrowSuccess) {
            throw new BusinessException("借阅失败，图书库存不足");
        }
        
        // 创建借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(borrowDays));
        record.setStatus(0); // 借阅中
        
        boolean saveSuccess = this.save(record);
        if (saveSuccess) {
            // 清除统计缓存
            clearStatsCache();
        }
        
        return saveSuccess;
    }

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
        
        // 归还图书
        boolean returnSuccess = bookInfoService.returnBook(record.getBookId(), record.getUserId());
        if (!returnSuccess) {
            throw new BusinessException("归还失败");
        }
        
        // 更新借阅记录
        record.setReturnDate(LocalDate.now());
        
        // 检查是否逾期
        if (LocalDate.now().isAfter(record.getDueDate())) {
            record.setStatus(2); // 逾期归还
        } else {
            record.setStatus(1); // 正常归还
        }
        
        boolean updateSuccess = this.updateById(record);
        if (updateSuccess) {
            clearStatsCache();
        }
        
        return updateSuccess;
    }

    @Override
    public Integer getBorrowingCount(Long userId) {
        return baseMapper.selectBorrowingCount(userId);
    }

    @Override
    public Integer getOverdueCount() {
        return baseMapper.selectOverdueCount(LocalDate.now());
    }

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
    
    private void clearStatsCache() {
        redisTemplate.delete("stats:monthlyBorrow");
        redisTemplate.delete("stats:hotBooks");
        redisTemplate.delete("stats:totalBorrowCount");
    }
}
