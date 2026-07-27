package com.bancopago.backend.application.primaryports.interactor;

import reactor.core.publisher.Mono;

/**
 * Contrato genérico. Preferir método de negocio explícito en la interfaz concreta ({@code blockAccount}).
 */
@FunctionalInterface
public interface InteractorWithoutReturn<I> {
    Mono<Void> execute(I input);
}
