package com.bancopago.backend.application.primaryports.interactor;

import reactor.core.publisher.Mono;

/**
 * Generic technical contract for a primary-port interactor with no return payload.
 * Concrete interactors MUST use an explicit business method name (e.g. {@code blockAccount}).
 */
@FunctionalInterface
public interface InteractorWithoutReturn<I> {
    Mono<Void> execute(I input);
}
