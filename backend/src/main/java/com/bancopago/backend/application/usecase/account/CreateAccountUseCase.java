package com.bancopago.backend.application.usecase.account;

import com.bancopago.backend.domain.account.AccountDomain;
import reactor.core.publisher.Mono;

public interface CreateAccountUseCase {

    Mono<AccountDomain> createAccount(CreateAccountCommand command);
}
