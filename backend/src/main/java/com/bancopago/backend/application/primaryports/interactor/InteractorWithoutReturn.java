package com.bancopago.backend.application.primaryports.interactor;

import reactor.core.publisher.Mono;

/**
 * Interactor con entrada y sin salida (DTO).
 * La interfaz concreta solo extiende esta base; la impl implementa {@link #execute}.
 */
@FunctionalInterface
public interface InteractorWithoutReturn<I> {
    Mono<Void> execute(I input);
}
