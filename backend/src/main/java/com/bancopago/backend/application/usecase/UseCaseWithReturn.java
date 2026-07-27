package com.bancopago.backend.application.usecase;

import reactor.core.publisher.Mono;

/**
 * Contrato genérico. Las interfaces concretas deben exponer un método de negocio explícito
 * ({@code createAccount}, {@code getAccountBalance}); {@code execute} es solo la forma funcional compartida.
 */
@FunctionalInterface
public interface UseCaseWithReturn<I, O> {
    Mono<O> execute(I input);
}
