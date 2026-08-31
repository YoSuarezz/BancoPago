package com.bancopago.backend.application.secondaryports.service;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

public interface IdempotencyService {

    Mono<Boolean> exists(String key);

    Mono<Void> store(String key, UUID transferId, Duration ttl);

    Mono<UUID> getTransferId(String key);
}
