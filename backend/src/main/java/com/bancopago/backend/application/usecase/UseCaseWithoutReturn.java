package com.bancopago.backend.application.usecase;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface UseCaseWithoutReturn<I> {
    Mono<Void> execute(I input);
}
