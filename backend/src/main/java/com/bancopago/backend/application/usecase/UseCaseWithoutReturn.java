package com.bancopago.backend.application.usecase;

import reactor.core.publisher.Mono;

/**
 * Contrato genérico. Preferir método de negocio explícito en la interfaz concreta ({@code blockAccount}).
 */
@FunctionalInterface
public interface UseCaseWithoutReturn<I> {
    Mono<Void> execute(I input);
}
