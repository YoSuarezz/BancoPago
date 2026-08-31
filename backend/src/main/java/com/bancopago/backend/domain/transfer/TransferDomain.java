package com.bancopago.backend.domain.transfer;

import com.bancopago.backend.crosscutting.helpers.ObjectHelper;
import com.bancopago.backend.crosscutting.helpers.TextHelper;
import com.bancopago.backend.domain.BaseDomain;
import com.bancopago.backend.domain.enums.Currency;
import com.bancopago.backend.domain.enums.TransferStatus;
import com.bancopago.backend.domain.transfer.exceptions.InvalidTransferException;
import com.bancopago.backend.domain.transfer.exceptions.SameAccountTransferException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransferDomain extends BaseDomain {

    private static final int MAX_DESCRIPTION_LENGTH = 200;

    private final String sourceAccountNumber;
    private final String targetAccountNumber;
    private final BigDecimal amount;
    private final Currency currency;
    private TransferStatus status;
    private final String description;
    private final String idempotencyKey;
    private final LocalDateTime createdAt;

    public TransferDomain(UUID id, String sourceAccountNumber, String targetAccountNumber,
                          BigDecimal amount, Currency currency, TransferStatus status,
                          String description, String idempotencyKey, LocalDateTime createdAt) {
        super(id);
        this.sourceAccountNumber = validateRequired(sourceAccountNumber, TransferError.SOURCE_REQUIRED);
        this.targetAccountNumber = validateRequired(targetAccountNumber, TransferError.TARGET_REQUIRED);
        validateDifferentAccounts(this.sourceAccountNumber, this.targetAccountNumber);
        this.amount = validatePositiveAmount(amount);
        this.currency = ObjectHelper.getDefault(currency, Currency.COP);
        this.status = ObjectHelper.getDefault(status, TransferStatus.PENDING);
        this.description = validateDescription(description);
        this.idempotencyKey = validateRequired(idempotencyKey, TransferError.IDEMPOTENCY_KEY_REQUIRED);
        this.createdAt = ObjectHelper.getDefault(createdAt, LocalDateTime.now());
    }

    public TransferDomain(String sourceAccountNumber, String targetAccountNumber,
                          BigDecimal amount, String description, String idempotencyKey) {
        this(null, sourceAccountNumber, targetAccountNumber, amount, null, null,
                description, idempotencyKey, null);
    }

    public void complete() {
        if (this.status != TransferStatus.PENDING) {
            throw InvalidTransferException.create(TransferError.INVALID_STATUS_TRANSITION,
                    this.status, TransferStatus.COMPLETED);
        }
        this.status = TransferStatus.COMPLETED;
    }

    public void fail() {
        if (this.status != TransferStatus.PENDING) {
            throw InvalidTransferException.create(TransferError.INVALID_STATUS_TRANSITION,
                    this.status, TransferStatus.FAILED);
        }
        this.status = TransferStatus.FAILED;
    }

    private static String validateRequired(String value, TransferError error) {
        String trimmed = TextHelper.applyTrim(value);
        if (TextHelper.isBlank(trimmed)) {
            throw InvalidTransferException.create(error);
        }
        return trimmed;
    }

    private static BigDecimal validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw InvalidTransferException.create(TransferError.INVALID_AMOUNT);
        }
        return amount;
    }

    private static void validateDifferentAccounts(String source, String target) {
        if (source.equals(target)) {
            throw SameAccountTransferException.create();
        }
    }

    private static String validateDescription(String description) {
        if (description == null) return null;
        String trimmed = TextHelper.applyTrim(description);
        if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
            throw InvalidTransferException.create(TransferError.DESCRIPTION_TOO_LONG, MAX_DESCRIPTION_LENGTH);
        }
        return trimmed.isEmpty() ? null : trimmed;
    }

    public String getSourceAccountNumber() { return sourceAccountNumber; }
    public String getTargetAccountNumber() { return targetAccountNumber; }
    public BigDecimal getAmount() { return amount; }
    public Currency getCurrency() { return currency; }
    public TransferStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
