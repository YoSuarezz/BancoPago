package com.bancopago.backend.application.usecase;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface RulesValidator<T> {
    Mono<Void> validate(T data);
}
