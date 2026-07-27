package com.bancopago.backend.application.primaryports.interactor;

import reactor.core.publisher.Mono;

/**
 * Contrato genérico. Las interfaces concretas deben exponer un método de negocio explícito
 * ({@code createAccount}); {@code execute} es solo la forma funcional compartida.
 */
@FunctionalInterface
public interface InteractorWithReturn<I, O> {
    Mono<O> execute(I input);
}
