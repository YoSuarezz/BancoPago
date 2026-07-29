package com.bancopago.backend.application.usecase;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface UseCaseWithReturn<I, O> {
    Mono<O> execute(I input);
}
