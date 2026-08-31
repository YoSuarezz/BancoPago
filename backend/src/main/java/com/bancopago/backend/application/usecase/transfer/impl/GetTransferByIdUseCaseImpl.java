package com.bancopago.backend.application.usecase.transfer.impl;

import com.bancopago.backend.application.secondaryports.repository.TransferRepository;
import com.bancopago.backend.application.usecase.transfer.GetTransferByIdUseCase;
import com.bancopago.backend.domain.transfer.TransferDomain;
import com.bancopago.backend.domain.transfer.exceptions.TransferNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class GetTransferByIdUseCaseImpl implements GetTransferByIdUseCase {

    private final TransferRepository transferRepository;

    public GetTransferByIdUseCaseImpl(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    @Override
    public Mono<TransferDomain> execute(UUID transferId) {
        return transferRepository.findTransferById(transferId)
                .switchIfEmpty(Mono.error(TransferNotFoundException.create(transferId)));
    }
}
