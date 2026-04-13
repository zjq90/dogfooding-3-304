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
        this.pages = calculatePages(total, size);
    }

    public PageResult(Long total, List<T> records, long current, long size) {
        this(total, records, Long.valueOf(current), Long.valueOf(size));
    }

    private Long calculatePages(Long total, Long size) {
        if (total == null || size == null || size <= 0) {
            return 0L;
        }
        return (total + size - 1) / size;
    }
}
