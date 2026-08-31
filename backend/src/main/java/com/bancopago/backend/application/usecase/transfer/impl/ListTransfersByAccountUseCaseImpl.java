package com.bancopago.backend.application.usecase.transfer.impl;

import com.bancopago.backend.application.secondaryports.repository.TransferRepository;
import com.bancopago.backend.application.usecase.transfer.ListTransfersByAccountUseCase;
import com.bancopago.backend.domain.transfer.TransferDomain;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ListTransfersByAccountUseCaseImpl implements ListTransfersByAccountUseCase {

    private final TransferRepository transferRepository;

    public ListTransfersByAccountUseCaseImpl(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    @Override
    public Flux<TransferDomain> execute(String accountNumber) {
        return transferRepository.findTransfersByAccountNumber(accountNumber);
    }
}
