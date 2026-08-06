package com.bancopago.backend.application.usecase;

import reactor.core.publisher.Flux;

@FunctionalInterface
public interface UseCaseWithFluxReturn<I, O> {
    Flux<O> execute(I input);
}
