package com.library.common;

import lombok.Data;
import java.util.List;

/**
 * 分页结果统一封装类
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> {
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页数据列表
     */
    private List<T> records;
    
    /**
     * 当前页码
     */
    private Long current;
    
    /**
     * 每页大小
     */
    private Long size;
    
    /**
     * 总页数
     */
    private Long pages;
    
    /**
     * 无参构造函数
     */
    public PageResult() {}
    
    /**
     * 带参构造函数
     * @param total 总记录数
     * @param records 当前页数据
     * @param current 当前页码
     * @param size 每页大小
     */
    public PageResult(long total, List<T> records, long current, long size) {
        this.total = total;
        this.records = records;
        this.current = current;
        this.size = size;
        this.pages = size > 0 ? (total + size - 1) / size : 0;
    }
}
