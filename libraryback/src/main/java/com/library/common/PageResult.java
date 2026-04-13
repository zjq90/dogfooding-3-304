package com.library.common;

import lombok.Data;
import java.util.List;

/**
 * 分页结果封装类
 * 用于统一返回分页数据格式
 */
@Data
public class PageResult<T> {
    
    private Long total;
    private List<T> records;
    private Long current;
    private Long size;
    private Long pages;
    
    public PageResult() {}
    
    public PageResult(Long total, List<T> records, Long current, Long size) {
        this.total = total;
        this.records = records;
        this.current = current;
        this.size = size;
        this.pages = (size != null && size > 0) ? (total + size - 1) / size : 0L;
    }
}
