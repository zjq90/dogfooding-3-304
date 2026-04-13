package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.BookInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 图书信息数据访问层
 */
@Mapper
public interface BookInfoMapper extends BaseMapper<BookInfo> {
    
    Page<BookInfo> selectBookPage(Page<BookInfo> page, 
                                   @Param("keyword") String keyword, 
                                   @Param("categoryId") Long categoryId);
    
    @Select("SELECT b.*, c.name as category_name FROM book_info b " +
            "LEFT JOIN book_category c ON b.category_id = c.id " +
            "WHERE b.id = #{id} AND b.deleted = 0")
    BookInfo selectBookDetail(@Param("id") Long id);
    
    @Update("UPDATE book_info SET available_quantity = available_quantity + #{delta}, " +
            "update_time = NOW() " +
            "WHERE id = #{id} AND available_quantity + #{delta} >= 0 AND deleted = 0")
    int updateAvailableQuantity(@Param("id") Long id, @Param("delta") int delta);
    
    @Select("SELECT COUNT(*) FROM book_info WHERE deleted = 0")
    Long selectBookCount();
    
    @Select("SELECT COALESCE(SUM(total_quantity), 0) FROM book_info WHERE deleted = 0")
    Long selectTotalBookQuantity();
    
    @Select("SELECT b.category_id, c.name as category_name, COUNT(*) as count " +
            "FROM book_info b LEFT JOIN book_category c ON b.category_id = c.id " +
            "WHERE b.deleted = 0 GROUP BY b.category_id ORDER BY count DESC")
    List<java.util.Map<String, Object>> selectCategoryStats();
}
