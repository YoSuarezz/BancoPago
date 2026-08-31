package com.bancopago.backend.domain.transfer.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.transfer.TransferError;

import java.io.Serial;

public class DuplicateIdempotencyKeyException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    private DuplicateIdempotencyKeyException(String key) {
        super(TransferError.DUPLICATE_IDEMPOTENCY_KEY, key);
    }

    public static DuplicateIdempotencyKeyException create(String key) {
        return new DuplicateIdempotencyKeyException(key);
    }
}
