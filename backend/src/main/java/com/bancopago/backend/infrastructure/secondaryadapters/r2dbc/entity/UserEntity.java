package com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.entity;

import com.bancopago.backend.crosscutting.helpers.ObjectHelper;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("app_user")
public class UserEntity implements Persistable<UUID> {

    @Id
    private UUID id;
    private String email;
    @Column("password_hash")
    private String passwordHash;
    private String role;
    @Column("person_id")
    private UUID personId;
    @Column("created_at")
    private LocalDateTime createdAt;

    @Transient
    private boolean newEntity = true;

    public UserEntity() {
        setId(UUID.randomUUID());
        setCreatedAt(LocalDateTime.now());
        this.newEntity = true;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = ObjectHelper.getDefault(id, UUID.randomUUID()); }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public UUID getPersonId() { return personId; }
    public void setPersonId(UUID personId) { this.personId = personId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = ObjectHelper.getDefault(createdAt, LocalDateTime.now()); }

    @Override
    public boolean isNew() { return newEntity; }

    public void markNew() { this.newEntity = true; }
    public void markPersisted() { this.newEntity = false; }
}
