package com.bancopago.backend.domain.transfer.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.transfer.TransferError;

import java.io.Serial;

public class InvalidTransferException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    private InvalidTransferException(TransferError error, Object... args) {
        super(error, args);
    }

    public static InvalidTransferException create(TransferError error, Object... args) {
        return new InvalidTransferException(error, args);
    }
}
