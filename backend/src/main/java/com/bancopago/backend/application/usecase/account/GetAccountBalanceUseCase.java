package com.bancopago.backend.application.usecase.account;

import com.bancopago.backend.domain.account.AccountDomain;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetAccountBalanceUseCase {

    Mono<AccountDomain> getAccountBalance(UUID accountId);
}
