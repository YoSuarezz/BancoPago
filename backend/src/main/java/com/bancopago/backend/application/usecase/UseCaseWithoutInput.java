package com.bancopago.backend.application.usecase;

import reactor.core.publisher.Mono;

/**
 * Generic technical contract for a use case with no input.
 * <p>
 * Concrete use case interfaces MUST declare an explicit business method
 * (e.g. {@code listActiveAccounts}) instead of exposing only {@code execute}.
 */
@FunctionalInterface
public interface UseCaseWithoutInput<O> {
    Mono<O> execute();
}
