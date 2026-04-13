package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 借阅记录实体类
 * 记录图书借阅和归还信息
 */
@Data
@TableName("borrow_record")
public class BorrowRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    @TableField(exist = false)
    private String username;
    
    @TableField(exist = false)
    private String realName;
    
    private Long bookId;
    
    @TableField(exist = false)
    private String bookTitle;
    
    @TableField(exist = false)
    private String bookIsbn;
    
    private LocalDate borrowDate;
    
    private LocalDate dueDate;
    
    private LocalDate returnDate;
    
    private Integer status;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField(select = false)
    private Integer deleted;
}
