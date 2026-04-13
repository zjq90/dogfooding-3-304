package com.library.common;

import lombok.Data;
import java.util.List;

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
        this.pages = size != null && size > 0 ? (total + size - 1) / size : 0;
    }
    
    public PageResult(Long total, List<T> records, long current, long size) {
        this.total = total;
        this.records = records;
        this.current = current;
        this.size = size;
        this.pages = size > 0 ? (total + size - 1) / size : 0;
    }
}
