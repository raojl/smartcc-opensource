package com.smt.refactor.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 通用分页结果
 */
@Data
@AllArgsConstructor
public class PageResult<T> {

    private List<T> list;
    private long total;
    private long page;
    private long size;
    private long totalPages;
}
