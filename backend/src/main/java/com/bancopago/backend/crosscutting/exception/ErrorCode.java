package com.bancopago.backend.crosscutting.exception;

public interface ErrorCode {
    String getCode();
    String getMessageTemplate();
}
