package com.bancopago.backend.application.usecase.account;

import com.bancopago.backend.domain.account.AccountDomain;
import reactor.core.publisher.Mono;

/**
 * Entrada: {@link ChangeAccountStatusCommand} (aún no hay Domain cargado).
 * Salida: {@link AccountDomain} actualizado.
 */
public interface ChangeAccountStatusUseCase {

    Mono<AccountDomain> changeAccountStatus(ChangeAccountStatusCommand command);
}
