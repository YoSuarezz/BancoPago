package com.bancopago.backend.application.secondaryports.repository;

import com.bancopago.backend.domain.account.AccountDomain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AccountRepository {

    Mono<AccountDomain> saveAccount(AccountDomain account);

    Mono<AccountDomain> findAccountById(UUID accountId);

    Mono<AccountDomain> findAccountByNumber(String accountNumber);

    Flux<AccountDomain> findAccountsByOwnerId(UUID ownerId);

    Mono<Boolean> existsAccountByNumber(String accountNumber);
}
