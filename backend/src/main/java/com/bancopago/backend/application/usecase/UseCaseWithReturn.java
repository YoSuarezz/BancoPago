package com.bancopago.backend.application.usecase;

import reactor.core.publisher.Mono;

/**
 * Generic technical contract for a use case that returns a value.
 * <p>
 * Concrete use case interfaces MUST declare an <strong>explicit business method</strong>
 * that names the action and the subject (e.g. {@code createAccount}, {@code getAccountBalance}).
 * Do not expose a public API method named only {@code execute} on concrete use cases —
 * {@code execute} exists here only as the shared functional shape for composition/generics.
 * <p>
 * Example:
 * <pre>{@code
 * public interface CreateAccountUseCase {
 *     Mono<AccountResponse> createAccount(CreateAccountRequest request);
 * }
 * }</pre>
 */
@FunctionalInterface
public interface UseCaseWithReturn<I, O> {
    Mono<O> execute(I input);
}
