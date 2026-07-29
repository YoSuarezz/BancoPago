package com.bancopago.backend.infrastructure;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<String> messages
) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, List.of(message));
    }

    public static ErrorResponse of(String code, String message, List<String> messages) {
        return new ErrorResponse(code, message, messages);
    }
}
