package com.bancopago.backend.application.usecase;

import reactor.core.publisher.Mono;

/**
 * Generic technical contract for a use case with no return payload.
 * <p>
 * Concrete use case interfaces MUST declare an explicit business method
 * (e.g. {@code blockAccount}, {@code closeAccount}) instead of exposing only {@code execute}.
 */
@FunctionalInterface
public interface UseCaseWithoutReturn<I> {
    Mono<Void> execute(I input);
}
