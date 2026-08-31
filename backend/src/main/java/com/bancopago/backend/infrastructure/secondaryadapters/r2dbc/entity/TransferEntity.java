package com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.entity;

import com.bancopago.backend.crosscutting.helpers.ObjectHelper;
import com.bancopago.backend.crosscutting.helpers.TextHelper;
import com.bancopago.backend.domain.enums.TransferStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("transfer")
public class TransferEntity implements Persistable<UUID> {

    @Id
    private UUID id;
    @Column("source_account_number")
    private String sourceAccountNumber;
    @Column("target_account_number")
    private String targetAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String description;
    @Column("idempotency_key")
    private String idempotencyKey;
    @Version
    private Long version;
    @Column("created_at")
    private LocalDateTime createdAt;

    @Transient
    private boolean newEntity = true;

    public TransferEntity() {
        setId(UUID.randomUUID());
        setSourceAccountNumber(TextHelper.EMPTY);
        setTargetAccountNumber(TextHelper.EMPTY);
        setAmount(BigDecimal.ZERO);
        setCurrency("COP");
        setStatus(TransferStatus.PENDING.name());
        setIdempotencyKey(TextHelper.EMPTY);
        setVersion(0L);
        setCreatedAt(LocalDateTime.now());
        this.newEntity = true;
    }

    public static TransferEntity create(UUID id, String sourceAccountNumber, String targetAccountNumber,
                                         BigDecimal amount, String currency, String status,
                                         String description, String idempotencyKey,
                                         Long version, LocalDateTime createdAt) {
        TransferEntity entity = new TransferEntity();
        entity.setId(id);
        entity.setSourceAccountNumber(sourceAccountNumber);
        entity.setTargetAccountNumber(targetAccountNumber);
        entity.setAmount(amount);
        entity.setCurrency(currency);
        entity.setStatus(status);
        entity.setDescription(description);
        entity.setIdempotencyKey(idempotencyKey);
        entity.setVersion(version);
        entity.setCreatedAt(createdAt);
        return entity;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = ObjectHelper.getDefault(id, UUID.randomUUID()); }

    public String getSourceAccountNumber() { return TextHelper.applyTrim(sourceAccountNumber); }
    public void setSourceAccountNumber(String sourceAccountNumber) { this.sourceAccountNumber = TextHelper.applyTrim(sourceAccountNumber); }

    public String getTargetAccountNumber() { return TextHelper.applyTrim(targetAccountNumber); }
    public void setTargetAccountNumber(String targetAccountNumber) { this.targetAccountNumber = TextHelper.applyTrim(targetAccountNumber); }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = ObjectHelper.getDefault(amount, BigDecimal.ZERO); }

    public String getCurrency() { return TextHelper.applyTrim(currency); }
    public void setCurrency(String currency) { this.currency = TextHelper.applyTrim(currency); }

    public String getStatus() { return TextHelper.applyTrim(status); }
    public void setStatus(String status) { this.status = TextHelper.applyTrim(status); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIdempotencyKey() { return TextHelper.applyTrim(idempotencyKey); }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = TextHelper.applyTrim(idempotencyKey); }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = ObjectHelper.getDefault(version, 0L); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = ObjectHelper.getDefault(createdAt, LocalDateTime.now()); }

    @Override
    public boolean isNew() { return newEntity; }

    public void markNew() { this.newEntity = true; }

    public void markPersisted() { this.newEntity = false; }
}
