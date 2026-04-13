package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 图书信息实体类
 * 对应数据库表book_info
 */
@Data
@TableName("book_info")
public class BookInfo {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * ISBN书号
     */
    private String isbn;
    
    /**
     * 书名
     */
    private String title;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * 出版社
     */
    private String publisher;
    
    /**
     * 出版日期
     */
    private LocalDate publishDate;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 分类名称（非数据库字段）
     */
    @TableField(exist = false)
    private String categoryName;
    
    /**
     * 图书描述
     */
    private String description;
    
    /**
     * 封面图片URL
     */
    private String coverImage;
    
    /**
     * 价格
     */
    private BigDecimal price;
    
    /**
     * 总数量
     */
    private Integer totalQuantity;
    
    /**
     * 可借数量
     */
    private Integer availableQuantity;
    
    /**
     * 馆藏位置
     */
    private String location;
    
    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;
    
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
