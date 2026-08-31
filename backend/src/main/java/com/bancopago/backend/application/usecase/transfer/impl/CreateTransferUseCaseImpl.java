package com.bancopago.backend.application.usecase.transfer.impl;

import com.bancopago.backend.application.secondaryports.repository.TransferRepository;
import com.bancopago.backend.application.secondaryports.service.AccountTransferOperationService;
import com.bancopago.backend.application.secondaryports.service.IdempotencyService;
import com.bancopago.backend.application.usecase.transfer.CreateTransferUseCase;
import com.bancopago.backend.application.usecase.transfer.TransferCommand;
import com.bancopago.backend.application.usecase.transfer.rulesvalidator.TransferRulesValidator;
import com.bancopago.backend.domain.transfer.TransferDomain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class CreateTransferUseCaseImpl implements CreateTransferUseCase {

    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);

    private final TransferRepository transferRepository;
    private final AccountTransferOperationService accountOperationService;
    private final IdempotencyService idempotencyService;
    private final TransferRulesValidator rulesValidator;

    public CreateTransferUseCaseImpl(TransferRepository transferRepository,
                                      AccountTransferOperationService accountOperationService,
                                      IdempotencyService idempotencyService,
                                      TransferRulesValidator rulesValidator) {
        this.transferRepository = transferRepository;
        this.accountOperationService = accountOperationService;
        this.idempotencyService = idempotencyService;
        this.rulesValidator = rulesValidator;
    }

    @Override
    @Transactional
    public Mono<TransferDomain> execute(TransferCommand command) {
        return idempotencyService.getTransferId(command.idempotencyKey())
                .flatMap(transferRepository::findTransferById)
                .switchIfEmpty(Mono.defer(() -> {
                    var transfer = new TransferDomain(
                            command.sourceAccountNumber(),
                            command.targetAccountNumber(),
                            command.amount(),
                            command.description(),
                            command.idempotencyKey()
                    );
                    return rulesValidator.validate(transfer)
                            .then(Mono.defer(() -> accountOperationService.executeTransfer(
                                    command.sourceAccountNumber(),
                                    command.targetAccountNumber(),
                                    command.amount())))
                            .then(Mono.defer(() -> {
                                transfer.complete();
                                return transferRepository.saveTransfer(transfer);
                            }))
                            .flatMap(saved -> idempotencyService.store(
                                    command.idempotencyKey(), saved.getId(), IDEMPOTENCY_TTL
                            ).thenReturn(saved));
                }));
    }
}
