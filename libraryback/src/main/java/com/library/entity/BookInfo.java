package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 图书信息实体类
 * 存储图书基本信息和库存信息
 */
@Data
@TableName("book_info")
public class BookInfo {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String isbn;
    
    private String title;
    
    private String author;
    
    private String publisher;
    
    private LocalDate publishDate;
    
    private Long categoryId;
    
    @TableField(exist = false)
    private String categoryName;
    
    private String description;
    
    private String coverImage;
    
    private BigDecimal price;
    
    private Integer totalQuantity;
    
    private Integer availableQuantity;
    
    private String location;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField(select = false)
    private Integer deleted;
}
