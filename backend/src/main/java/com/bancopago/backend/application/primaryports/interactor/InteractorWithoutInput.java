package com.bancopago.backend.application.primaryports.interactor;

import reactor.core.publisher.Mono;

/**
 * Contrato genérico. Preferir método de negocio explícito en la interfaz concreta ({@code listActiveAccounts}).
 */
@FunctionalInterface
public interface InteractorWithoutInput<O> {
    Mono<O> execute();
}
