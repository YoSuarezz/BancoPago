package com.bancopago.backend.application.secondaryports.repository;

import com.bancopago.backend.domain.account.AccountDomain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AccountRepository {
    Mono<AccountDomain> save(AccountDomain account);
    Mono<AccountDomain> findById(UUID id);
    Mono<AccountDomain> findByNumber(String number);
    Flux<AccountDomain> findByOwnerId(UUID ownerId);
    Mono<Boolean> existsByNumber(String number);
}
