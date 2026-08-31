package com.bancopago.backend.domain.transfer;

import com.bancopago.backend.domain.enums.Currency;
import com.bancopago.backend.domain.enums.TransferStatus;
import com.bancopago.backend.domain.transfer.exceptions.InvalidTransferException;
import com.bancopago.backend.domain.transfer.exceptions.SameAccountTransferException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransferTest {

    private static final String SOURCE = "5300000001";
    private static final String TARGET = "5300000002";
    private static final String IDEMPOTENCY_KEY = "key-123";

    @Nested
    @DisplayName("Transfer creation")
    class TransferCreation {

        @Test
        @DisplayName("should create transfer with PENDING status and COP currency")
        void shouldCreateTransferWithDefaults() {
            var transfer = new TransferDomain(SOURCE, TARGET, new BigDecimal("100.00"), "Test transfer", IDEMPOTENCY_KEY);

            assertEquals(TransferStatus.PENDING, transfer.getStatus());
            assertEquals(Currency.COP, transfer.getCurrency());
            assertEquals(SOURCE, transfer.getSourceAccountNumber());
            assertEquals(TARGET, transfer.getTargetAccountNumber());
            assertEquals(new BigDecimal("100.00"), transfer.getAmount());
            assertEquals("Test transfer", transfer.getDescription());
            assertEquals(IDEMPOTENCY_KEY, transfer.getIdempotencyKey());
            assertNotNull(transfer.getId());
            assertNotNull(transfer.getCreatedAt());
        }

        @Test
        @DisplayName("should create transfer with null description")
        void shouldCreateTransferWithNullDescription() {
            var transfer = new TransferDomain(SOURCE, TARGET, new BigDecimal("50.00"), null, IDEMPOTENCY_KEY);

            assertNull(transfer.getDescription());
        }

        @Test
        @DisplayName("should throw when source equals target")
        void shouldThrowWhenSameAccount() {
            assertThrows(SameAccountTransferException.class,
                    () -> new TransferDomain(SOURCE, SOURCE, new BigDecimal("100.00"), null, IDEMPOTENCY_KEY));
        }

        @Test
        @DisplayName("should throw when amount is zero")
        void shouldThrowWhenAmountIsZero() {
            assertThrows(InvalidTransferException.class,
                    () -> new TransferDomain(SOURCE, TARGET, BigDecimal.ZERO, null, IDEMPOTENCY_KEY));
        }

        @Test
        @DisplayName("should throw when amount is negative")
        void shouldThrowWhenAmountIsNegative() {
            assertThrows(InvalidTransferException.class,
                    () -> new TransferDomain(SOURCE, TARGET, new BigDecimal("-10.00"), null, IDEMPOTENCY_KEY));
        }

        @Test
        @DisplayName("should throw when amount is null")
        void shouldThrowWhenAmountIsNull() {
            assertThrows(InvalidTransferException.class,
                    () -> new TransferDomain(SOURCE, TARGET, null, null, IDEMPOTENCY_KEY));
        }

        @Test
        @DisplayName("should throw when source is blank")
        void shouldThrowWhenSourceIsBlank() {
            assertThrows(InvalidTransferException.class,
                    () -> new TransferDomain("", TARGET, new BigDecimal("100.00"), null, IDEMPOTENCY_KEY));
        }

        @Test
        @DisplayName("should throw when target is blank")
        void shouldThrowWhenTargetIsBlank() {
            assertThrows(InvalidTransferException.class,
                    () -> new TransferDomain(SOURCE, "", new BigDecimal("100.00"), null, IDEMPOTENCY_KEY));
        }

        @Test
        @DisplayName("should throw when idempotency key is blank")
        void shouldThrowWhenIdempotencyKeyIsBlank() {
            assertThrows(InvalidTransferException.class,
                    () -> new TransferDomain(SOURCE, TARGET, new BigDecimal("100.00"), null, ""));
        }

        @Test
        @DisplayName("should throw when description exceeds 200 characters")
        void shouldThrowWhenDescriptionTooLong() {
            String longDescription = "a".repeat(201);
            assertThrows(InvalidTransferException.class,
                    () -> new TransferDomain(SOURCE, TARGET, new BigDecimal("100.00"), longDescription, IDEMPOTENCY_KEY));
        }

        @Test
        @DisplayName("should trim source and target account numbers")
        void shouldTrimAccountNumbers() {
            var transfer = new TransferDomain("  " + SOURCE + "  ", "  " + TARGET + "  ",
                    new BigDecimal("100.00"), null, IDEMPOTENCY_KEY);

            assertEquals(SOURCE, transfer.getSourceAccountNumber());
            assertEquals(TARGET, transfer.getTargetAccountNumber());
        }
    }

    @Nested
    @DisplayName("Status transitions")
    class StatusTransitions {

        @Test
        @DisplayName("should transition from PENDING to COMPLETED")
        void shouldCompleteFromPending() {
            var transfer = new TransferDomain(SOURCE, TARGET, new BigDecimal("100.00"), null, IDEMPOTENCY_KEY);

            transfer.complete();

            assertEquals(TransferStatus.COMPLETED, transfer.getStatus());
        }

        @Test
        @DisplayName("should transition from PENDING to FAILED")
        void shouldFailFromPending() {
            var transfer = new TransferDomain(SOURCE, TARGET, new BigDecimal("100.00"), null, IDEMPOTENCY_KEY);

            transfer.fail();

            assertEquals(TransferStatus.FAILED, transfer.getStatus());
        }

        @Test
        @DisplayName("should throw when completing an already completed transfer")
        void shouldThrowWhenCompletingCompleted() {
            var transfer = new TransferDomain(SOURCE, TARGET, new BigDecimal("100.00"), null, IDEMPOTENCY_KEY);
            transfer.complete();

            assertThrows(InvalidTransferException.class, transfer::complete);
        }

        @Test
        @DisplayName("should throw when failing an already completed transfer")
        void shouldThrowWhenFailingCompleted() {
            var transfer = new TransferDomain(SOURCE, TARGET, new BigDecimal("100.00"), null, IDEMPOTENCY_KEY);
            transfer.complete();

            assertThrows(InvalidTransferException.class, transfer::fail);
        }

        @Test
        @DisplayName("should throw when completing an already failed transfer")
        void shouldThrowWhenCompletingFailed() {
            var transfer = new TransferDomain(SOURCE, TARGET, new BigDecimal("100.00"), null, IDEMPOTENCY_KEY);
            transfer.fail();

            assertThrows(InvalidTransferException.class, transfer::complete);
        }

        @Test
        @DisplayName("should throw when failing an already failed transfer")
        void shouldThrowWhenFailingFailed() {
            var transfer = new TransferDomain(SOURCE, TARGET, new BigDecimal("100.00"), null, IDEMPOTENCY_KEY);
            transfer.fail();

            assertThrows(InvalidTransferException.class, transfer::fail);
        }
    }
}
