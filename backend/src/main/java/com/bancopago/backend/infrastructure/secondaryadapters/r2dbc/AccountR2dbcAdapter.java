package com.bancopago.backend.infrastructure.secondaryadapters.r2dbc;

import com.bancopago.backend.application.secondaryports.entity.AccountEntity;
import com.bancopago.backend.application.secondaryports.mapper.AccountEntityMapper;
import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.domain.account.AccountDomain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class AccountR2dbcAdapter implements AccountRepository {

    private final AccountR2dbcRepository accountR2dbcRepository;
    private final AccountEntityMapper accountEntityMapper;

    public AccountR2dbcAdapter(AccountR2dbcRepository accountR2dbcRepository,
                               AccountEntityMapper accountEntityMapper) {
        this.accountR2dbcRepository = accountR2dbcRepository;
        this.accountEntityMapper = accountEntityMapper;
    }

    @Override
    public Mono<AccountDomain> saveAccount(AccountDomain account) {
        return accountR2dbcRepository.findById(account.getId())
                .flatMap(existing -> {
                    AccountEntity entity = accountEntityMapper.toEntity(account);
                    entity.setCreatedAt(existing.getCreatedAt());
                    entity.setVersion(existing.getVersion());
                    entity.markPersisted();
                    return accountR2dbcRepository.save(entity);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    AccountEntity entity = accountEntityMapper.toEntity(account);
                    entity.markNew();
                    return accountR2dbcRepository.save(entity);
                }))
                .map(accountEntityMapper::toDomain);
    }

    @Override
    public Mono<AccountDomain> findAccountById(UUID accountId) {
        return accountR2dbcRepository.findById(accountId).map(accountEntityMapper::toDomain);
    }

    @Override
    public Mono<AccountDomain> findAccountByNumber(String accountNumber) {
        return accountR2dbcRepository.findByAccountNumber(accountNumber)
                .map(accountEntityMapper::toDomain);
    }

    @Override
    public Flux<AccountDomain> findAccountsByOwnerId(UUID ownerId) {
        return accountR2dbcRepository.findByPersonId(ownerId).map(accountEntityMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsAccountByNumber(String accountNumber) {
        return accountR2dbcRepository.existsByAccountNumber(accountNumber);
    }
}
