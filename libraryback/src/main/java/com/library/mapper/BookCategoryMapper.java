package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BookCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 图书分类数据访问层
 */
@Mapper
public interface BookCategoryMapper extends BaseMapper<BookCategory> {
    
    @Select("SELECT * FROM book_category WHERE deleted = 0 ORDER BY sort_order ASC, id ASC")
    List<BookCategory> selectAllCategories();
    
    @Select("SELECT name FROM book_category WHERE id = #{id} AND deleted = 0")
    String selectNameById(@Param("id") Long id);
}
