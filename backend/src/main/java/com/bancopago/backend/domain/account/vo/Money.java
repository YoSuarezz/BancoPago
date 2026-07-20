package com.bancopago.backend.domain.account.vo;

import com.bancopago.backend.crosscutting.helpers.ObjectHelper;
import com.bancopago.backend.domain.account.AccountError;
import com.bancopago.backend.domain.account.exceptions.InsufficientBalanceException;
import com.bancopago.backend.domain.account.exceptions.InvalidAccountException;
import com.bancopago.backend.domain.account.exceptions.InvalidAmountException;
import com.bancopago.backend.domain.enums.Currency;

import java.math.BigDecimal;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw InvalidAmountException.create(amount);
        }
        currency = ObjectHelper.getDefault(currency, Currency.COP);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO, Currency.COP);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw InvalidAccountException.create(AccountError.CURRENCY_MISMATCH, this.currency, other.currency);
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw InvalidAccountException.create(AccountError.CURRENCY_MISMATCH, this.currency, other.currency);
        }
        if (this.amount.compareTo(other.amount) < 0) {
            throw InsufficientBalanceException.create(this.amount, other.amount);
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
}
