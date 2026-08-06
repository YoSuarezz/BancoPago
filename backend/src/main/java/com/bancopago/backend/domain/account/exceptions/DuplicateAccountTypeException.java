package com.bancopago.backend.domain.account.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.account.AccountError;
import com.bancopago.backend.domain.enums.AccountType;

import java.io.Serial;
import java.util.UUID;

public class DuplicateAccountTypeException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID ownerId;
    private final AccountType accountType;

    private DuplicateAccountTypeException(UUID ownerId, AccountType accountType) {
        super(AccountError.DUPLICATE_ACCOUNT_TYPE, ownerId, accountType);
        this.ownerId = ownerId;
        this.accountType = accountType;
    }

    public static DuplicateAccountTypeException create(UUID ownerId, AccountType accountType) {
        return new DuplicateAccountTypeException(ownerId, accountType);
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public AccountType getAccountType() {
        return accountType;
    }
}
