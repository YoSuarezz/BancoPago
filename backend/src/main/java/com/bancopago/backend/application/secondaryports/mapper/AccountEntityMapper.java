package com.bancopago.backend.application.secondaryports.mapper;

import com.bancopago.backend.domain.account.AccountDomain;
import com.bancopago.backend.domain.account.vo.AccountNumber;
import com.bancopago.backend.domain.account.vo.Money;
import com.bancopago.backend.domain.enums.AccountStatus;
import com.bancopago.backend.domain.enums.AccountType;
import com.bancopago.backend.domain.enums.Currency;
import com.bancopago.backend.application.secondaryports.entity.AccountEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AccountEntityMapper {

    public AccountEntity toEntity(AccountDomain domain) {
        if (domain == null) return null;
        return AccountEntity.create(
                domain.getId(),
                domain.getOwnerId(),
                domain.getNumber(),
                mapAccountType(domain.getType()),
                domain.getBalance(),
                mapCurrency(domain.getCurrency()),
                mapAccountStatus(domain.getStatus()),
                0L, null
        );
    }

    public AccountDomain toDomain(AccountEntity entity) {
        if (entity == null) return null;
        return new AccountDomain(
                entity.getId(),
                entity.getPersonaId(),
                new AccountNumber(entity.getNumero()),
                mapAccountType(entity.getTipo()),
                new Money(entity.getSaldo(), mapCurrency(entity.getMoneda())),
                mapAccountStatus(entity.getEstado())
        );
    }

    public List<AccountEntity> toEntityCollection(List<AccountDomain> domainList) {
        if (domainList == null) return List.of();
        return domainList.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<AccountDomain> toDomainCollection(List<AccountEntity> entityList) {
        if (entityList == null) return List.of();
        return entityList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    private String mapAccountType(AccountType type) {
        return type != null ? type.name() : null;
    }

    private AccountType mapAccountType(String value) {
        if (value == null) return null;
        try { return AccountType.valueOf(value.toUpperCase()); } catch (IllegalArgumentException e) { return null; }
    }

    private String mapCurrency(Currency currency) {
        return currency != null ? currency.name() : null;
    }

    private Currency mapCurrency(String value) {
        if (value == null) return null;
        try { return Currency.valueOf(value.toUpperCase()); } catch (IllegalArgumentException e) { return null; }
    }

    private String mapAccountStatus(AccountStatus status) {
        return status != null ? status.name() : null;
    }

    private AccountStatus mapAccountStatus(String value) {
        if (value == null) return null;
        try { return AccountStatus.valueOf(value.toUpperCase()); } catch (IllegalArgumentException e) { return null; }
    }
}
