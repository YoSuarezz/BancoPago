package com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.entity;

import com.bancopago.backend.crosscutting.helpers.ObjectHelper;
import com.bancopago.backend.crosscutting.helpers.TextHelper;
import com.bancopago.backend.domain.enums.AccountStatus;
import com.bancopago.backend.domain.enums.Currency;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("account")
public class AccountEntity implements Persistable<UUID> {

    @Id
    private UUID id;
    @Column("person_id")
    private UUID personId;
    @Column("account_number")
    private String accountNumber;
    @Column("account_type")
    private String accountType;
    private BigDecimal balance;
    private String currency;
    private String status;
    @Version
    private Long version;
    @Column("created_at")
    private LocalDateTime createdAt;

    @Transient
    private boolean newEntity = true;

    public AccountEntity() {
        setId(UUID.randomUUID());
        setAccountNumber(TextHelper.EMPTY);
        setAccountType(TextHelper.EMPTY);
        setBalance(BigDecimal.ZERO);
        setCurrency(Currency.COP.name());
        setStatus(AccountStatus.ACTIVE.name());
        setVersion(0L);
        setCreatedAt(LocalDateTime.now());
        this.newEntity = true;
    }

    public static AccountEntity create() {
        return new AccountEntity();
    }

    public static AccountEntity create(UUID id, UUID personId, String accountNumber,
                                       String accountType, BigDecimal balance, String currency,
                                       String status, Long version, LocalDateTime createdAt) {
        AccountEntity entity = new AccountEntity();
        entity.setId(id);
        entity.setPersonId(personId);
        entity.setAccountNumber(accountNumber);
        entity.setAccountType(accountType);
        entity.setBalance(balance);
        entity.setCurrency(currency);
        entity.setStatus(status);
        entity.setVersion(version);
        entity.setCreatedAt(createdAt);
        return entity;
    }

    public static AccountEntity create(UUID personId, String accountNumber, String accountType) {
        return create(UUID.randomUUID(), personId, accountNumber, accountType,
                BigDecimal.ZERO, Currency.COP.name(), AccountStatus.ACTIVE.name(), 0L, LocalDateTime.now());
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = ObjectHelper.getDefault(id, UUID.randomUUID()); }

    public UUID getPersonId() { return personId; }
    public void setPersonId(UUID personId) { this.personId = personId; }

    public String getAccountNumber() { return TextHelper.applyTrim(accountNumber); }
    public void setAccountNumber(String accountNumber) { this.accountNumber = TextHelper.applyTrim(accountNumber); }

    public String getAccountType() { return TextHelper.applyTrim(accountType); }
    public void setAccountType(String accountType) { this.accountType = TextHelper.applyTrim(accountType); }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = ObjectHelper.getDefault(balance, BigDecimal.ZERO); }

    public String getCurrency() { return TextHelper.applyTrim(currency); }
    public void setCurrency(String currency) { this.currency = TextHelper.applyTrim(currency); }

    public String getStatus() { return TextHelper.applyTrim(status); }
    public void setStatus(String status) { this.status = TextHelper.applyTrim(status); }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = ObjectHelper.getDefault(version, 0L); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = ObjectHelper.getDefault(createdAt, LocalDateTime.now()); }

    @Override
    public boolean isNew() { return newEntity; }

    public void markNew() { this.newEntity = true; }

    public void markPersisted() { this.newEntity = false; }
}
