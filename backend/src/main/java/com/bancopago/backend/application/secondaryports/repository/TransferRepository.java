package com.bancopago.backend.application.secondaryports.repository;

import com.bancopago.backend.domain.transfer.TransferDomain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TransferRepository {

    Mono<TransferDomain> saveTransfer(TransferDomain transfer);

    Mono<TransferDomain> findTransferById(UUID transferId);

    Flux<TransferDomain> findTransfersByAccountNumber(String accountNumber);

    Mono<Boolean> existsByIdempotencyKey(String idempotencyKey);
}
