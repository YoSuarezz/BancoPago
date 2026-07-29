package com.bancopago.backend.infrastructure.primaryadapters.adapter.response;

import java.util.List;

public class ApiResponse<T> extends Response<T> {

    public static <T> ApiResponse<T> of(T item) {
        ApiResponse<T> response = new ApiResponse<>();
        if (item != null) {
            response.getData().add(item);
        }
        return response;
    }

    public static <T> ApiResponse<T> of(List<T> items) {
        ApiResponse<T> response = new ApiResponse<>();
        if (items != null) {
            response.setData(items);
        }
        return response;
    }

    public static <T> ApiResponse<T> message(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        if (message != null) {
            response.getMessages().add(message);
        }
        return response;
    }
}
