package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 借阅记录实体类
 * 对应数据库表borrow_record
 */
@Data
@TableName("borrow_record")
public class BorrowRecord {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名（非数据库字段，关联查询）
     */
    @TableField(exist = false)
    private String username;
    
    /**
     * 真实姓名（非数据库字段，关联查询）
     */
    @TableField(exist = false)
    private String realName;
    
    /**
     * 图书ID
     */
    private Long bookId;
    
    /**
     * 图书名称（非数据库字段，关联查询）
     */
    @TableField(exist = false)
    private String bookTitle;
    
    /**
     * 图书ISBN（非数据库字段，关联查询）
     */
    @TableField(exist = false)
    private String bookIsbn;
    
    /**
     * 借阅日期
     */
    private LocalDate borrowDate;
    
    /**
     * 应还日期
     */
    private LocalDate dueDate;
    
    /**
     * 实际归还日期
     */
    private LocalDate returnDate;
    
    /**
     * 状态：0-借阅中，1-已归还，2-逾期归还
     */
    private Integer status;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    @TableLogic
    @TableField(select = false)
    private Integer deleted;
}
