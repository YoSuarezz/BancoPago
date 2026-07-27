package com.bancopago.backend.application.primaryports.interactor;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface InteractorWithoutInput<O> {
    Mono<O> execute();
}
