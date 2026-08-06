package com.bancopago.backend.domain.enums;

public enum AccountOperation {
    BLOCK("block"),
    UNBLOCK("unblock"),
    CLOSE("close"),
    OPERATE("operate");

    private final String displayName;

    AccountOperation(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
