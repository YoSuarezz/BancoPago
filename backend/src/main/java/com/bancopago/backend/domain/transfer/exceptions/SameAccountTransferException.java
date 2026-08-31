package com.bancopago.backend.domain.transfer.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.transfer.TransferError;

import java.io.Serial;

public class SameAccountTransferException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    private SameAccountTransferException() {
        super(TransferError.SAME_ACCOUNT);
    }

    public static SameAccountTransferException create() {
        return new SameAccountTransferException();
    }
}
