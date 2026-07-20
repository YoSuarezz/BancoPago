package com.bancopago.backend.domain;

import java.util.UUID;

public abstract class BaseDomain {
    private final UUID id;

    protected BaseDomain(UUID id) {
        this.id = id != null ? id : UUID.randomUUID();
    }

    public UUID getId() { return id; }
}
