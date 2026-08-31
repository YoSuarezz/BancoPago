package com.bancopago.backend.domain.transfer.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.transfer.TransferError;

import java.io.Serial;
import java.util.UUID;

public class TransferNotFoundException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID transferId;

    private TransferNotFoundException(UUID transferId) {
        super(TransferError.NOT_FOUND, transferId);
        this.transferId = transferId;
    }

    public static TransferNotFoundException create(UUID transferId) {
        return new TransferNotFoundException(transferId);
    }

    public UUID getTransferId() {
        return transferId;
    }
}
