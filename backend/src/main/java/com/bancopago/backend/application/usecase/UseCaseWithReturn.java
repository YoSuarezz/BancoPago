package com.bancopago.backend.application.usecase;

import reactor.core.publisher.Mono;

/**
 * Contrato genérico técnico. Las interfaces concretas
 * ({@code CreateAccountUseCase}) deben exponer un método de negocio
 * explícito ({@code createAccount}), no {@code execute}.
 */
@FunctionalInterface
public interface UseCaseWithReturn<I, O> {
    Mono<O> execute(I input);
}
