package com.bancopago.backend.application.primaryports.interactor;

import reactor.core.publisher.Mono;

/**
 * Generic technical contract for a primary-port interactor that returns a value.
 * <p>
 * Concrete interactors MUST expose an explicit business method name
 * (e.g. {@code createAccount}, {@code getAccountBalance}) on their interface.
 * {@code execute} is only the shared functional shape for generics/composition.
 */
@FunctionalInterface
public interface InteractorWithReturn<I, O> {
    Mono<O> execute(I input);
}
