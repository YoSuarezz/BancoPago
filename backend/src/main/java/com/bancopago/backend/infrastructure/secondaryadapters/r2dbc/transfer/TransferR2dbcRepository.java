package com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.transfer;

import com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.entity.TransferEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface TransferR2dbcRepository extends ReactiveCrudRepository<TransferEntity, UUID> {

    Flux<TransferEntity> findBySourceAccountNumberOrTargetAccountNumberOrderByCreatedAtDesc(
            String sourceAccountNumber, String targetAccountNumber);

    Mono<Boolean> existsByIdempotencyKey(String idempotencyKey);
}
