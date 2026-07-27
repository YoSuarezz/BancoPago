package com.bancopago.backend.application.usecase;

import reactor.core.publisher.Mono;

/**
 * Contrato genérico. Preferir método de negocio explícito en la interfaz concreta ({@code listActiveAccounts}).
 */
@FunctionalInterface
public interface UseCaseWithoutInput<O> {
    Mono<O> execute();
}
