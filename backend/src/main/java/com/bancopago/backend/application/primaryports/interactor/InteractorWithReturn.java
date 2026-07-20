package com.bancopago.backend.application.primaryports.interactor;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface InteractorWithReturn<I, O> {
    Mono<O> execute(I input);
}
