package com.bancopago.backend.application.secondaryports.mapper;

import com.bancopago.backend.application.secondaryports.entity.AccountEntity;
import com.bancopago.backend.domain.account.AccountDomain;
import com.bancopago.backend.domain.account.vo.AccountNumber;
import com.bancopago.backend.domain.account.vo.Money;
import com.bancopago.backend.domain.enums.AccountStatus;
import com.bancopago.backend.domain.enums.AccountType;
import com.bancopago.backend.domain.enums.Currency;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AccountEntityMapper {

    public AccountEntity toEntity(AccountDomain domain) {
        if (domain == null) {
            return null;
        }
        AccountEntity entity = AccountEntity.create(
                domain.getId(),
                domain.getOwnerId(),
                domain.getNumber(),
                mapAccountType(domain.getType()),
                domain.getBalance(),
                mapCurrency(domain.getCurrency()),
                mapAccountStatus(domain.getStatus()),
                0L,
                null
        );
        entity.markNew();
        return entity;
    }

    public AccountDomain toDomain(AccountEntity entity) {
        if (entity == null) {
            return null;
        }
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
        if (domainList == null) {
            return List.of();
        }
        return domainList.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<AccountDomain> toDomainCollection(List<AccountEntity> entityList) {
        if (entityList == null) {
            return List.of();
        }
        return entityList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    private String mapAccountType(AccountType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case CHECKING -> "CORRIENTE";
            case SAVINGS -> "AHORROS";
            case PAYROLL -> "NOMINA";
            case TREASURY -> "TESORERIA";
            case SUPPLIER -> "PROVEEDOR";
        };
    }

    private AccountType mapAccountType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return switch (value.trim().toUpperCase()) {
            case "CORRIENTE", "CHECKING" -> AccountType.CHECKING;
            case "AHORROS", "SAVINGS" -> AccountType.SAVINGS;
            case "NOMINA", "PAYROLL" -> AccountType.PAYROLL;
            case "TESORERIA", "TREASURY" -> AccountType.TREASURY;
            case "PROVEEDOR", "SUPPLIER" -> AccountType.SUPPLIER;
            default -> null;
        };
    }

    private String mapCurrency(Currency currency) {
        return currency != null ? currency.name() : null;
    }

    private Currency mapCurrency(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Currency.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String mapAccountStatus(AccountStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case ACTIVE -> "ACTIVA";
            case INACTIVE -> "INACTIVA";
            case BLOCKED -> "BLOQUEADA";
            case SEIZED -> "EMBARGADA";
        };
    }

    private AccountStatus mapAccountStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return switch (value.trim().toUpperCase()) {
            case "ACTIVA", "ACTIVE" -> AccountStatus.ACTIVE;
            case "INACTIVA", "INACTIVE" -> AccountStatus.INACTIVE;
            case "BLOQUEADA", "BLOCKED" -> AccountStatus.BLOCKED;
            case "EMBARGADA", "SEIZED" -> AccountStatus.SEIZED;
            default -> null;
        };
    }
}
