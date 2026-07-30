package com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.mapper;

import com.bancopago.backend.crosscutting.helpers.TextHelper;
import com.bancopago.backend.domain.account.AccountDomain;
import com.bancopago.backend.domain.account.AccountError;
import com.bancopago.backend.domain.account.exceptions.InvalidAccountException;
import com.bancopago.backend.domain.account.vo.AccountNumber;
import com.bancopago.backend.domain.account.vo.Money;
import com.bancopago.backend.domain.enums.AccountStatus;
import com.bancopago.backend.domain.enums.AccountType;
import com.bancopago.backend.domain.enums.Currency;
import com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.entity.AccountEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountEntityMapper {

    public AccountEntity toAccountEntity(AccountDomain domain) {
        if (domain == null) {
            return null;
        }
        AccountEntity entity = AccountEntity.create(
                domain.getId(),
                domain.getOwnerId(),
                domain.getNumber(),
                requireEnumName(domain.getType()),
                domain.getBalance(),
                domain.getCurrency() != null ? domain.getCurrency().name() : Currency.COP.name(),
                domain.getStatus() != null ? domain.getStatus().name() : AccountStatus.ACTIVE.name(),
                0L,
                null
        );
        entity.markNew();
        return entity;
    }

    public AccountDomain toAccountDomain(AccountEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AccountDomain(
                entity.getId(),
                entity.getPersonId(),
                new AccountNumber(entity.getAccountNumber()),
                parseAccountType(entity.getAccountType()),
                new Money(entity.getBalance(), parseCurrency(entity.getCurrency())),
                parseAccountStatus(entity.getStatus())
        );
    }

    private String requireEnumName(Enum<?> value) {
        if (value == null) {
            throw InvalidAccountException.create(AccountError.TYPE_REQUIRED);
        }
        return value.name();
    }

    private AccountType parseAccountType(String value) {
        if (TextHelper.isBlank(value)) {
            throw InvalidAccountException.create(AccountError.TYPE_REQUIRED);
        }
        try {
            return AccountType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw InvalidAccountException.create(AccountError.TYPE_REQUIRED);
        }
    }

    private Currency parseCurrency(String value) {
        if (TextHelper.isBlank(value)) {
            return Currency.COP;
        }
        try {
            return Currency.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Currency.COP;
        }
    }

    private AccountStatus parseAccountStatus(String value) {
        if (TextHelper.isBlank(value)) {
            return AccountStatus.ACTIVE;
        }
        try {
            return AccountStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw InvalidAccountException.create(AccountError.INVALID_STATE, "reconstitute", "unknown", value);
        }
    }
}
