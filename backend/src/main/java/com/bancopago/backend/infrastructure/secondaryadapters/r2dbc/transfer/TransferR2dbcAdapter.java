package com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.transfer;

import com.bancopago.backend.application.secondaryports.repository.TransferRepository;
import com.bancopago.backend.domain.transfer.TransferDomain;
import com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.entity.TransferEntity;
import com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.mapper.TransferEntityMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class TransferR2dbcAdapter implements TransferRepository {

    private final TransferR2dbcRepository transferR2dbcRepository;
    private final TransferEntityMapper transferEntityMapper;

    public TransferR2dbcAdapter(TransferR2dbcRepository transferR2dbcRepository,
                                TransferEntityMapper transferEntityMapper) {
        this.transferR2dbcRepository = transferR2dbcRepository;
        this.transferEntityMapper = transferEntityMapper;
    }

    @Override
    public Mono<TransferDomain> saveTransfer(TransferDomain transfer) {
        return transferR2dbcRepository.findById(transfer.getId())
                .flatMap(existing -> {
                    TransferEntity entity = transferEntityMapper.toTransferEntity(transfer);
                    entity.setCreatedAt(existing.getCreatedAt());
                    entity.setVersion(existing.getVersion());
                    entity.markPersisted();
                    return transferR2dbcRepository.save(entity);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    TransferEntity entity = transferEntityMapper.toTransferEntity(transfer);
                    entity.markNew();
                    return transferR2dbcRepository.save(entity);
                }))
                .map(transferEntityMapper::toTransferDomain);
    }

    @Override
    public Mono<TransferDomain> findTransferById(UUID transferId) {
        return transferR2dbcRepository.findById(transferId).map(transferEntityMapper::toTransferDomain);
    }

    @Override
    public Flux<TransferDomain> findTransfersByAccountNumber(String accountNumber) {
        return transferR2dbcRepository
                .findBySourceAccountNumberOrTargetAccountNumberOrderByCreatedAtDesc(accountNumber, accountNumber)
                .map(transferEntityMapper::toTransferDomain);
    }

    @Override
    public Mono<Boolean> existsByIdempotencyKey(String idempotencyKey) {
        return transferR2dbcRepository.existsByIdempotencyKey(idempotencyKey);
    }
}
