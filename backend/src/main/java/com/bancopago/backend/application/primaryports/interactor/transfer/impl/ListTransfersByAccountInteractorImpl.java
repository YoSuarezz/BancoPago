package com.bancopago.backend.application.primaryports.interactor.transfer.impl;

import com.bancopago.backend.application.primaryports.dto.transfer.response.ListTransferResponse;
import com.bancopago.backend.application.primaryports.interactor.transfer.ListTransfersByAccountInteractor;
import com.bancopago.backend.application.primaryports.mapper.transfer.TransferDTOMapper;
import com.bancopago.backend.application.usecase.transfer.ListTransfersByAccountUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ListTransfersByAccountInteractorImpl implements ListTransfersByAccountInteractor {

    private final ListTransfersByAccountUseCase listTransfersByAccountUseCase;
    private final TransferDTOMapper transferDTOMapper;

    public ListTransfersByAccountInteractorImpl(ListTransfersByAccountUseCase listTransfersByAccountUseCase,
                                                 TransferDTOMapper transferDTOMapper) {
        this.listTransfersByAccountUseCase = listTransfersByAccountUseCase;
        this.transferDTOMapper = transferDTOMapper;
    }

    @Override
    public Mono<List<ListTransferResponse>> execute(String accountNumber) {
        return listTransfersByAccountUseCase.execute(accountNumber)
                .map(transferDTOMapper::toListTransferResponse)
                .collectList();
    }
}
