package com.bancopago.backend.crosscutting.exception;

public abstract class DomainException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Layer layer;
    private final Object[] args;
    private final String userMessage;

    protected DomainException(ErrorCode errorCode, Layer layer, Object... args) {
        super(String.format(errorCode.getMessageTemplate(), args));
        this.errorCode = errorCode;
        this.layer = layer;
        this.args = args;
        this.userMessage = getMessage();
    }

    protected DomainException(ErrorCode errorCode, Object... args) {
        this(errorCode, Layer.DOMAIN, args);
    }

    public String getCode() { return errorCode.getCode(); }
    public ErrorCode getErrorCode() { return errorCode; }
    public Layer getLayer() { return layer; }
    public Object[] getArgs() { return args; }
    public String getUserMessage() { return userMessage; }
}
