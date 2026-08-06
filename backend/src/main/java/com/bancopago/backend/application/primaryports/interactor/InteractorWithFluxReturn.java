package com.bancopago.backend.application.primaryports.interactor;

import reactor.core.publisher.Flux;

@FunctionalInterface
public interface InteractorWithFluxReturn<I, O> {
    Flux<O> execute(I input);
}
