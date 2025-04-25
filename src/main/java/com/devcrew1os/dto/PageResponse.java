package com.devcrew1os.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResponse<T> {
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private Long totalElements;
    private boolean isLast;
    private List<T> content;

    public PageResponse(Page<T> page) {
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.isLast = page.isLast();
        this.content = page.getContent();
    }
}
