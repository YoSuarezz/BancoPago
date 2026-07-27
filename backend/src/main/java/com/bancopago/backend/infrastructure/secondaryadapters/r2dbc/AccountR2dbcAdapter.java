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

    private final AccountR2dbcRepository repository;
    private final AccountEntityMapper mapper;

    public AccountR2dbcAdapter(AccountR2dbcRepository repository, AccountEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<AccountDomain> save(AccountDomain account) {
        return repository.findById(account.getId())
                .flatMap(existing -> {
                    AccountEntity entity = mapper.toEntity(account);
                    entity.setCreatedAt(existing.getCreatedAt());
                    entity.setVersion(existing.getVersion());
                    entity.markPersisted();
                    return repository.save(entity);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    AccountEntity entity = mapper.toEntity(account);
                    entity.markNew();
                    return repository.save(entity);
                }))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<AccountDomain> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<AccountDomain> findByNumber(String number) {
        return repository.findByNumero(number).map(mapper::toDomain);
    }

    @Override
    public Flux<AccountDomain> findByOwnerId(UUID ownerId) {
        return repository.findByPersonaId(ownerId).map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByNumber(String number) {
        return repository.existsByNumero(number);
    }
}
