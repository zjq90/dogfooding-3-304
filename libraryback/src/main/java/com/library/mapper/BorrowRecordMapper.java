package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 借阅记录数据访问层
 */
@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {
    
    Page<BorrowRecord> selectBorrowPage(Page<BorrowRecord> page,
                                         @Param("userId") Long userId,
                                         @Param("bookId") Long bookId,
                                         @Param("status") Integer status);
    
    @Select("SELECT br.*, u.username, u.real_name, b.title as book_title, b.isbn as book_isbn " +
            "FROM borrow_record br " +
            "LEFT JOIN sys_user u ON br.user_id = u.id " +
            "LEFT JOIN book_info b ON br.book_id = b.id " +
            "WHERE br.id = #{id} AND br.deleted = 0")
    BorrowRecord selectBorrowDetail(@Param("id") Long id);
    
    @Select("SELECT COUNT(*) FROM borrow_record WHERE user_id = #{userId} AND status = 0 AND deleted = 0")
    Integer selectBorrowingCount(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) FROM borrow_record WHERE status = 0 AND due_date < #{today} AND deleted = 0")
    Integer selectOverdueCount(@Param("today") LocalDate today);
    
    @Select("SELECT COUNT(*) FROM borrow_record WHERE user_id = #{userId} AND status = 0 AND due_date < #{today} AND deleted = 0")
    Integer selectUserOverdueCount(@Param("userId") Long userId, @Param("today") LocalDate today);
    
    @Select("SELECT DATE_FORMAT(borrow_date, '%Y-%m') as month, COUNT(*) as count " +
            "FROM borrow_record WHERE deleted = 0 " +
            "AND borrow_date >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
            "GROUP BY DATE_FORMAT(borrow_date, '%Y-%m') " +
            "ORDER BY month")
    List<Map<String, Object>> selectMonthlyBorrowStats();
    
    @Select("SELECT b.title as book_title, COUNT(*) as borrow_count " +
            "FROM borrow_record br LEFT JOIN book_info b ON br.book_id = b.id " +
            "WHERE br.deleted = 0 GROUP BY br.book_id ORDER BY borrow_count DESC LIMIT 10")
    List<Map<String, Object>> selectHotBooks();
    
    @Select("SELECT COUNT(*) FROM borrow_record WHERE deleted = 0")
    Long selectTotalBorrowCount();
}
