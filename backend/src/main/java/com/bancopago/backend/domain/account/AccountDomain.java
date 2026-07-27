package com.bancopago.backend.domain.account;

import com.bancopago.backend.crosscutting.helpers.ObjectHelper;
import com.bancopago.backend.domain.BaseDomain;
import com.bancopago.backend.domain.account.exceptions.AccountBlockedException;
import com.bancopago.backend.domain.account.exceptions.InvalidAccountException;
import com.bancopago.backend.domain.account.exceptions.InvalidAccountStateException;
import com.bancopago.backend.domain.account.exceptions.InvalidAmountException;
import com.bancopago.backend.domain.account.vo.AccountNumber;
import com.bancopago.backend.domain.account.vo.Money;
import com.bancopago.backend.domain.enums.AccountOperation;
import com.bancopago.backend.domain.enums.AccountStatus;
import com.bancopago.backend.domain.enums.AccountType;
import com.bancopago.backend.domain.enums.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountDomain extends BaseDomain {

    private final UUID ownerId;
    private final AccountNumber number;
    private final AccountType type;
    private Money balance;
    private AccountStatus status;

    public AccountDomain(UUID id, UUID ownerId, AccountNumber number, AccountType type,
                         Money balance, AccountStatus status) {
        super(id);
        this.ownerId = ObjectHelper.requireNonNull(ownerId, () -> InvalidAccountException.create(AccountError.OWNER_REQUIRED));
        this.number = ObjectHelper.requireNonNull(number, () -> InvalidAccountException.create(AccountError.NUMBER_EMPTY));
        this.type = ObjectHelper.requireNonNull(type, () -> InvalidAccountException.create(AccountError.TYPE_REQUIRED));
        this.balance = ObjectHelper.getDefault(balance, Money::zero);
        this.status = ObjectHelper.getDefault(status, AccountStatus.ACTIVE);
    }

    public AccountDomain(UUID ownerId, AccountNumber number, AccountType type) {
        this(null, ownerId, number, type, null, null);
    }

    public void deposit(BigDecimal amount) {
        ensureOperable();
        validatePositiveAmount(amount);
        this.balance = this.balance.add(new Money(amount, balance.currency()));
    }

    public void withdraw(BigDecimal amount) {
        ensureOperable();
        validatePositiveAmount(amount);
        this.balance = this.balance.subtract(new Money(amount, balance.currency()));
    }

    public void block() {
        if (this.status != AccountStatus.ACTIVE) {
            throw InvalidAccountStateException.create(getId(), this.status, AccountOperation.BLOCK);
        }
        this.status = AccountStatus.BLOCKED;
    }

    public void unblock() {
        if (this.status != AccountStatus.BLOCKED) {
            throw InvalidAccountStateException.create(getId(), this.status, AccountOperation.UNBLOCK);
        }
        this.status = AccountStatus.ACTIVE;
    }

    public void close() {
        if (this.status == AccountStatus.INACTIVE) {
            throw InvalidAccountStateException.create(getId(), this.status, AccountOperation.CLOSE);
        }
        this.status = AccountStatus.INACTIVE;
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw InvalidAmountException.create(amount);
        }
    }

    private void ensureOperable() {
        if (this.status == AccountStatus.BLOCKED) {
            throw AccountBlockedException.create(getId());
        }
        if (this.status == AccountStatus.INACTIVE) {
            throw InvalidAccountStateException.create(getId(), this.status, AccountOperation.OPERATE);
        }
    }

    public UUID getOwnerId() { return ownerId; }
    public AccountNumber getNumberObject() { return number; }
    public String getNumber() { return number.value(); }
    public AccountType getType() { return type; }
    public Currency getCurrency() { return balance.currency(); }
    public BigDecimal getBalance() { return balance.amount(); }
    public Money getMoney() { return balance; }
    public AccountStatus getStatus() { return status; }
}
