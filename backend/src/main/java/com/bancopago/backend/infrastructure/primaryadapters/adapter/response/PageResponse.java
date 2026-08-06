package com.bancopago.backend.infrastructure.primaryadapters.adapter.response;

import com.bancopago.backend.application.model.PageResult;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponse<T> from(PageResult<T> pageResult) {
        return new PageResponse<>(
                pageResult.content(),
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages()
        );
    }
}
