package com.bancopago.backend.application.primaryports.interactor;

import reactor.core.publisher.Mono;

/**
 * Generic technical contract for a primary-port interactor with no input.
 * Concrete interactors MUST use an explicit business method name (e.g. {@code listActiveAccounts}).
 */
@FunctionalInterface
public interface InteractorWithoutInput<O> {
    Mono<O> execute();
}
