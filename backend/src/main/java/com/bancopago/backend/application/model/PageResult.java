package com.bancopago.backend.application.model;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(List<T> content, long totalElements, int page, int size) {

    public int totalPages() {
        if (size <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / (double) size);
    }

    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(
                content.stream().map(mapper).toList(),
                totalElements,
                page,
                size
        );
    }
}
