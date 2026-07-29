package com.bancopago.backend.application.usecase;

import reactor.core.publisher.Mono;

/**
 * Regla de aplicación CON ESTADO (consulta repositorio).
 * Se compone dentro de un {@link RulesValidator}; el validator solo las ejecuta en orden.
 * <p>
 * No usar para invariantes puros (null, formato, rangos): eso va en VOs / dominio.
 */
@FunctionalInterface
public interface Rule<T> {
    Mono<Void> validate(T data);
}
