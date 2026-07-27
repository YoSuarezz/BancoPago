package com.bancopago.backend.application.primaryports.interactor;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface InteractorWithoutReturn<I> {
    Mono<Void> execute(I input);
}
