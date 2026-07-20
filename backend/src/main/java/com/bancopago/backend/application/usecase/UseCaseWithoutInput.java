package com.bancopago.backend.application.usecase;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface UseCaseWithoutInput<O> {
    Mono<O> execute();
}
