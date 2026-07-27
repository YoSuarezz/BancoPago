package com.bancopago.backend.application.secondaryports.entity;

import com.bancopago.backend.crosscutting.helpers.ObjectHelper;
import com.bancopago.backend.crosscutting.helpers.TextHelper;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("person")
public class PersonEntity implements Persistable<UUID> {

    @Id
    private UUID id;
    private String name;
    @Column("document_number")
    private String documentNumber;
    @Column("document_type")
    private String documentType;
    private String email;
    private String phone;
    @Column("person_type")
    private String personType;
    @Column("created_at")
    private LocalDateTime createdAt;

    @Transient
    private boolean newEntity = true;

    public PersonEntity() {
        setId(UUID.randomUUID());
        setName(TextHelper.EMPTY);
        setDocumentNumber(TextHelper.EMPTY);
        setDocumentType(TextHelper.EMPTY);
        setEmail(TextHelper.EMPTY);
        setPhone(TextHelper.EMPTY);
        setPersonType(TextHelper.EMPTY);
        setCreatedAt(LocalDateTime.now());
        this.newEntity = true;
    }

    public static PersonEntity create() {
        return new PersonEntity();
    }

    public static PersonEntity create(UUID id, String name, String documentNumber,
                                      String documentType, String email,
                                      String phone, String personType, LocalDateTime createdAt) {
        PersonEntity entity = new PersonEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setDocumentNumber(documentNumber);
        entity.setDocumentType(documentType);
        entity.setEmail(email);
        entity.setPhone(phone);
        entity.setPersonType(personType);
        entity.setCreatedAt(createdAt);
        return entity;
    }

    public static PersonEntity create(String name, String documentNumber,
                                      String documentType, String email,
                                      String phone, String personType) {
        return create(UUID.randomUUID(), name, documentNumber, documentType,
                email, phone, personType, LocalDateTime.now());
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = ObjectHelper.getDefault(id, UUID.randomUUID()); }

    public String getName() { return TextHelper.applyTrim(name); }
    public void setName(String name) { this.name = TextHelper.applyTrim(name); }

    public String getDocumentNumber() { return TextHelper.applyTrim(documentNumber); }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = TextHelper.applyTrim(documentNumber); }

    public String getDocumentType() { return TextHelper.applyTrim(documentType); }
    public void setDocumentType(String documentType) { this.documentType = TextHelper.applyTrim(documentType); }

    public String getEmail() { return TextHelper.applyTrim(email); }
    public void setEmail(String email) { this.email = TextHelper.applyTrim(email); }

    public String getPhone() { return TextHelper.applyTrim(phone); }
    public void setPhone(String phone) { this.phone = TextHelper.applyTrim(phone); }

    public String getPersonType() { return TextHelper.applyTrim(personType); }
    public void setPersonType(String personType) { this.personType = TextHelper.applyTrim(personType); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = ObjectHelper.getDefault(createdAt, LocalDateTime.now()); }

    @Override
    public boolean isNew() { return newEntity; }

    public void markNew() { this.newEntity = true; }

    public void markPersisted() { this.newEntity = false; }
}
