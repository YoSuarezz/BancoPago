package com.bancopago.backend.domain.account.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.account.AccountError;

import java.io.Serial;

public class DuplicateAccountException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String accountNumber;

    private DuplicateAccountException(String accountNumber) {
        super(AccountError.NUMBER_ALREADY_EXISTS, accountNumber);
        this.accountNumber = accountNumber;
    }

    public static DuplicateAccountException create(String accountNumber) {
        return new DuplicateAccountException(accountNumber);
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
