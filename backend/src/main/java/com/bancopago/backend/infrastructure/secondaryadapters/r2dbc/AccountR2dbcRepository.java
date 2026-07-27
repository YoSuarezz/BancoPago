package com.bancopago.backend.infrastructure.secondaryadapters.r2dbc;

import com.bancopago.backend.application.secondaryports.entity.AccountEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface AccountR2dbcRepository extends R2dbcRepository<AccountEntity, UUID> {

    Mono<AccountEntity> findByNumero(String numero);

    Flux<AccountEntity> findByPersonaId(UUID personaId);

    Mono<Boolean> existsByNumero(String numero);
}
