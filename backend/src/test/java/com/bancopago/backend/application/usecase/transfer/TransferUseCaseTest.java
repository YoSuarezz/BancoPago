package com.bancopago.backend.application.usecase.transfer;

import com.bancopago.backend.application.secondaryports.repository.TransferRepository;
import com.bancopago.backend.application.secondaryports.service.AccountTransferOperationService;
import com.bancopago.backend.application.secondaryports.service.IdempotencyService;
import com.bancopago.backend.application.usecase.transfer.impl.CreateTransferUseCaseImpl;
import com.bancopago.backend.application.usecase.transfer.impl.GetTransferByIdUseCaseImpl;
import com.bancopago.backend.application.usecase.transfer.impl.ListTransfersByAccountUseCaseImpl;
import com.bancopago.backend.application.usecase.transfer.rulesvalidator.TransferRulesValidator;
import com.bancopago.backend.domain.enums.Currency;
import com.bancopago.backend.domain.enums.TransferStatus;
import com.bancopago.backend.domain.transfer.TransferDomain;
import com.bancopago.backend.domain.transfer.exceptions.InvalidTransferException;
import com.bancopago.backend.domain.transfer.exceptions.TransferNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferUseCaseTest {

    @Mock
    private TransferRepository transferRepository;
    @Mock
    private AccountTransferOperationService accountOperationService;
    @Mock
    private IdempotencyService idempotencyService;
    @Mock
    private TransferRulesValidator rulesValidator;

    private CreateTransferUseCaseImpl createTransferUseCase;
    private GetTransferByIdUseCaseImpl getTransferByIdUseCase;
    private ListTransfersByAccountUseCaseImpl listTransfersByAccountUseCase;

    @BeforeEach
    void setUp() {
        createTransferUseCase = new CreateTransferUseCaseImpl(
                transferRepository, accountOperationService, idempotencyService, rulesValidator);
        getTransferByIdUseCase = new GetTransferByIdUseCaseImpl(transferRepository);
        listTransfersByAccountUseCase = new ListTransfersByAccountUseCaseImpl(transferRepository);
    }

    @Nested
    @DisplayName("CreateTransferUseCase")
    class CreateTransfer {

        @Test
        @DisplayName("should create transfer successfully")
        void shouldCreateTransferSuccessfully() {
            var command = new TransferCommand("1111111111", "2222222222",
                    new BigDecimal("100.00"), "Test transfer", "idem-key-1");

            when(idempotencyService.getTransferId("idem-key-1")).thenReturn(Mono.empty());
            when(rulesValidator.validate(any(TransferDomain.class))).thenReturn(Mono.empty());
            when(accountOperationService.executeTransfer(
                    eq("1111111111"), eq("2222222222"), any(BigDecimal.class)))
                    .thenReturn(Mono.empty());
            when(transferRepository.saveTransfer(any(TransferDomain.class)))
                    .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
            when(idempotencyService.store(eq("idem-key-1"), any(UUID.class), any()))
                    .thenReturn(Mono.empty());

            StepVerifier.create(createTransferUseCase.execute(command))
                    .assertNext(result -> {
                        assertNotNull(result.getId());
                        assertEquals("1111111111", result.getSourceAccountNumber());
                        assertEquals("2222222222", result.getTargetAccountNumber());
                        assertEquals(new BigDecimal("100.00"), result.getAmount());
                        assertEquals(TransferStatus.COMPLETED, result.getStatus());
                        assertEquals("idem-key-1", result.getIdempotencyKey());
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("should return existing transfer on idempotent replay")
        void shouldReturnExistingOnIdempotentReplay() {
            var existingTransfer = new TransferDomain(UUID.randomUUID(), "1111111111", "2222222222",
                    new BigDecimal("100.00"), Currency.COP, TransferStatus.COMPLETED,
                    "Test", "idem-key-1", null);

            when(idempotencyService.getTransferId("idem-key-1"))
                    .thenReturn(Mono.just(existingTransfer.getId()));
            when(transferRepository.findTransferById(existingTransfer.getId()))
                    .thenReturn(Mono.just(existingTransfer));

            var command = new TransferCommand("1111111111", "2222222222",
                    new BigDecimal("100.00"), "Test", "idem-key-1");

            StepVerifier.create(createTransferUseCase.execute(command))
                    .assertNext(result -> {
                        assertEquals(existingTransfer.getId(), result.getId());
                        assertEquals(TransferStatus.COMPLETED, result.getStatus());
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("should fail when rules validation fails")
        void shouldFailWhenValidationFails() {
            var command = new TransferCommand("1111111111", "2222222222",
                    new BigDecimal("100.00"), null, "idem-key-2");

            when(idempotencyService.getTransferId("idem-key-2")).thenReturn(Mono.empty());
            when(rulesValidator.validate(any(TransferDomain.class)))
                    .thenReturn(Mono.error(InvalidTransferException.create(
                            com.bancopago.backend.domain.transfer.TransferError.SOURCE_NOT_FOUND, "1111111111")));

            StepVerifier.create(createTransferUseCase.execute(command))
                    .expectError(InvalidTransferException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("GetTransferByIdUseCase")
    class GetTransferById {

        @Test
        @DisplayName("should return transfer when found")
        void shouldReturnTransferWhenFound() {
            var transfer = new TransferDomain(UUID.randomUUID(), "1111111111", "2222222222",
                    new BigDecimal("50.00"), Currency.COP, TransferStatus.COMPLETED,
                    null, "key-1", null);

            when(transferRepository.findTransferById(transfer.getId()))
                    .thenReturn(Mono.just(transfer));

            StepVerifier.create(getTransferByIdUseCase.execute(transfer.getId()))
                    .assertNext(result -> assertEquals(transfer.getId(), result.getId()))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should fail when transfer not found")
        void shouldFailWhenNotFound() {
            UUID missingId = UUID.randomUUID();
            when(transferRepository.findTransferById(missingId)).thenReturn(Mono.empty());

            StepVerifier.create(getTransferByIdUseCase.execute(missingId))
                    .expectError(TransferNotFoundException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("ListTransfersByAccountUseCase")
    class ListTransfersByAccount {

        @Test
        @DisplayName("should list transfers for account")
        void shouldListTransfersForAccount() {
            var t1 = new TransferDomain(UUID.randomUUID(), "1111111111", "2222222222",
                    new BigDecimal("100.00"), Currency.COP, TransferStatus.COMPLETED,
                    null, "key-1", null);
            var t2 = new TransferDomain(UUID.randomUUID(), "3333333333", "1111111111",
                    new BigDecimal("50.00"), Currency.COP, TransferStatus.COMPLETED,
                    null, "key-2", null);

            when(transferRepository.findTransfersByAccountNumber("1111111111"))
                    .thenReturn(Flux.just(t1, t2));

            StepVerifier.create(listTransfersByAccountUseCase.execute("1111111111"))
                    .expectNextCount(2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("should return empty when no transfers")
        void shouldReturnEmptyWhenNoTransfers() {
            when(transferRepository.findTransfersByAccountNumber("9999999999"))
                    .thenReturn(Flux.empty());

            StepVerifier.create(listTransfersByAccountUseCase.execute("9999999999"))
                    .verifyComplete();
        }
    }
}
