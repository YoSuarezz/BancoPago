package com.bancopago.backend.application.primaryports.interactor.transfer.impl;

import com.bancopago.backend.application.primaryports.dto.transfer.response.CreateTransferResponse;
import com.bancopago.backend.application.primaryports.interactor.transfer.CreateTransferInput;
import com.bancopago.backend.application.primaryports.interactor.transfer.CreateTransferInteractor;
import com.bancopago.backend.application.primaryports.mapper.transfer.TransferDTOMapper;
import com.bancopago.backend.application.usecase.transfer.CreateTransferUseCase;
import com.bancopago.backend.application.usecase.transfer.TransferCommand;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CreateTransferInteractorImpl implements CreateTransferInteractor {

    private final CreateTransferUseCase createTransferUseCase;
    private final TransferDTOMapper transferDTOMapper;

    public CreateTransferInteractorImpl(CreateTransferUseCase createTransferUseCase,
                                         TransferDTOMapper transferDTOMapper) {
        this.createTransferUseCase = createTransferUseCase;
        this.transferDTOMapper = transferDTOMapper;
    }

    @Override
    public Mono<CreateTransferResponse> execute(CreateTransferInput input) {
        var request = input.request();
        var command = new TransferCommand(
                request.sourceAccountNumber(),
                request.targetAccountNumber(),
                request.amount(),
                request.description(),
                input.idempotencyKey()
        );
        return createTransferUseCase.execute(command)
                .map(transferDTOMapper::toCreateTransferResponse);
    }
}
