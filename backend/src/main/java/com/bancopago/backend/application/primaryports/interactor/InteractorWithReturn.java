package com.bancopago.backend.application.primaryports.interactor;

import reactor.core.publisher.Mono;

/**
 * Contrato genérico técnico. Las interfaces concretas
 * ({@code GetAccountBalanceInteractor}) deben exponer un método de negocio
 * explícito ({@code getAccountBalance}), no {@code execute}.
 */
@FunctionalInterface
public interface InteractorWithReturn<I, O> {
    Mono<O> execute(I input);
}
