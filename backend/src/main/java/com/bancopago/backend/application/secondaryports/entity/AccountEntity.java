package com.bancopago.backend.application.secondaryports.entity;

import com.bancopago.backend.crosscutting.helpers.ObjectHelper;
import com.bancopago.backend.crosscutting.helpers.TextHelper;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("cuenta")
public class AccountEntity implements Persistable<UUID> {

    @Id
    private UUID id;
    @Column("persona_id")
    private UUID personaId;
    private String numero;
    private String tipo;
    private BigDecimal saldo;
    private String moneda;
    private String estado;
    @Version
    private Long version;
    @Column("created_at")
    private LocalDateTime createdAt;

    @Transient
    private boolean newEntity = true;

    public AccountEntity() {
        setId(UUID.randomUUID());
        setNumero(TextHelper.EMPTY);
        setTipo(TextHelper.EMPTY);
        setSaldo(BigDecimal.ZERO);
        setMoneda("COP");
        setEstado("ACTIVA");
        setVersion(0L);
        setCreatedAt(LocalDateTime.now());
        this.newEntity = true;
    }

    public static AccountEntity create() { return new AccountEntity(); }

    public static AccountEntity create(UUID id, UUID personaId, String numero,
                                        String tipo, BigDecimal saldo, String moneda,
                                        String estado, Long version, LocalDateTime createdAt) {
        AccountEntity entity = new AccountEntity();
        entity.setId(id);
        entity.setPersonaId(personaId);
        entity.setNumero(numero);
        entity.setTipo(tipo);
        entity.setSaldo(saldo);
        entity.setMoneda(moneda);
        entity.setEstado(estado);
        entity.setVersion(version);
        entity.setCreatedAt(createdAt);
        return entity;
    }

    public static AccountEntity create(UUID personaId, String numero, String tipo) {
        return create(UUID.randomUUID(), personaId, numero, tipo,
                BigDecimal.ZERO, "COP", "ACTIVA", 0L, LocalDateTime.now());
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = ObjectHelper.getDefault(id, UUID.randomUUID()); }

    public UUID getPersonaId() { return personaId; }
    public void setPersonaId(UUID personaId) { this.personaId = personaId; }

    public String getNumero() { return TextHelper.applyTrim(numero); }
    public void setNumero(String numero) { this.numero = TextHelper.applyTrim(numero); }

    public String getTipo() { return TextHelper.applyTrim(tipo); }
    public void setTipo(String tipo) { this.tipo = TextHelper.applyTrim(tipo); }

    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = ObjectHelper.getDefault(saldo, BigDecimal.ZERO); }

    public String getMoneda() { return TextHelper.applyTrim(moneda); }
    public void setMoneda(String moneda) { this.moneda = TextHelper.applyTrim(moneda); }

    public String getEstado() { return TextHelper.applyTrim(estado); }
    public void setEstado(String estado) { this.estado = TextHelper.applyTrim(estado); }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = ObjectHelper.getDefault(version, 0L); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = ObjectHelper.getDefault(createdAt, LocalDateTime.now()); }

    @Override
    public boolean isNew() { return newEntity; }

    public void markNew() { this.newEntity = true; }

    public void markPersisted() { this.newEntity = false; }
}
