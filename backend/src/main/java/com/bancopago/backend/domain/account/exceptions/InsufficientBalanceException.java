package com.bancopago.backend.domain.account.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.account.AccountError;
import java.math.BigDecimal;

public class InsufficientBalanceException extends DomainException {
    private static final long serialVersionUID = 1L;
    private final BigDecimal currentBalance;
    private final BigDecimal requiredAmount;

    private InsufficientBalanceException(BigDecimal currentBalance, BigDecimal requiredAmount) {
        super(AccountError.INSUFFICIENT_BALANCE, currentBalance, requiredAmount);
        this.currentBalance = currentBalance;
        this.requiredAmount = requiredAmount;
    }

    public static InsufficientBalanceException create(BigDecimal currentBalance, BigDecimal requiredAmount) {
        return new InsufficientBalanceException(currentBalance, requiredAmount);
    }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public BigDecimal getRequiredAmount() { return requiredAmount; }
}
