package com.bancopago.backend.domain.account.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.account.AccountError;
import java.math.BigDecimal;

public class InvalidAmountException extends DomainException {
    private static final long serialVersionUID = 1L;

    private InvalidAmountException(BigDecimal amount) {
        super(AccountError.INVALID_AMOUNT, amount);
    }

    public static InvalidAmountException create(BigDecimal amount) {
        return new InvalidAmountException(amount);
    }
}
