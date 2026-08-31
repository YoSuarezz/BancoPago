package com.bancopago.backend.application.primaryports.interactor.transfer.impl;

import com.bancopago.backend.application.primaryports.dto.transfer.response.GetTransferResponse;
import com.bancopago.backend.application.primaryports.interactor.transfer.GetTransferByIdInteractor;
import com.bancopago.backend.application.primaryports.mapper.transfer.TransferDTOMapper;
import com.bancopago.backend.application.usecase.transfer.GetTransferByIdUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class GetTransferByIdInteractorImpl implements GetTransferByIdInteractor {

    private final GetTransferByIdUseCase getTransferByIdUseCase;
    private final TransferDTOMapper transferDTOMapper;

    public GetTransferByIdInteractorImpl(GetTransferByIdUseCase getTransferByIdUseCase,
                                          TransferDTOMapper transferDTOMapper) {
        this.getTransferByIdUseCase = getTransferByIdUseCase;
        this.transferDTOMapper = transferDTOMapper;
    }

    @Override
    public Mono<GetTransferResponse> execute(UUID transferId) {
        return getTransferByIdUseCase.execute(transferId)
                .map(transferDTOMapper::toGetTransferResponse);
    }
}
