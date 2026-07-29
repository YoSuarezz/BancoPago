package com.bancopago.backend.application.secondaryports.entity;

import com.bancopago.backend.crosscutting.helpers.ObjectHelper;
import com.bancopago.backend.crosscutting.helpers.TextHelper;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
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
    @Column("client_number")
    private String clientNumber;
    @Column("membership_date")
    private LocalDate membershipDate;
    private String position;
    private String area;
    @Column("cost_center")
    private String costCenter;
    @Column("contract_type")
    private String contractType;
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
        setClientNumber(TextHelper.EMPTY);
        setPosition(TextHelper.EMPTY);
        setArea(TextHelper.EMPTY);
        setCostCenter(TextHelper.EMPTY);
        setContractType(TextHelper.EMPTY);
        setCreatedAt(LocalDateTime.now());
        this.newEntity = true;
    }

    public static PersonEntity create() {
        return new PersonEntity();
    }

    public static PersonEntity create(UUID id, String name, String documentNumber,
                                      String documentType, String email,
                                      String phone, String personType,
                                      String clientNumber, LocalDate membershipDate,
                                      String position, String area,
                                      String costCenter, String contractType,
                                      LocalDateTime createdAt) {
        PersonEntity entity = new PersonEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setDocumentNumber(documentNumber);
        entity.setDocumentType(documentType);
        entity.setEmail(email);
        entity.setPhone(phone);
        entity.setPersonType(personType);
        entity.setClientNumber(clientNumber);
        entity.setMembershipDate(membershipDate);
        entity.setPosition(position);
        entity.setArea(area);
        entity.setCostCenter(costCenter);
        entity.setContractType(contractType);
        entity.setCreatedAt(createdAt);
        return entity;
    }

    public static PersonEntity create(UUID id, String name, String documentNumber,
                                      String documentType, String email,
                                      String phone, String personType, LocalDateTime createdAt) {
        return create(id, name, documentNumber, documentType, email, phone, personType,
                TextHelper.EMPTY, null, TextHelper.EMPTY, TextHelper.EMPTY,
                TextHelper.EMPTY, TextHelper.EMPTY, createdAt);
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

    public String getClientNumber() { return TextHelper.applyTrim(clientNumber); }
    public void setClientNumber(String clientNumber) { this.clientNumber = TextHelper.applyTrim(clientNumber); }

    public LocalDate getMembershipDate() { return membershipDate; }
    public void setMembershipDate(LocalDate membershipDate) { this.membershipDate = membershipDate; }

    public String getPosition() { return TextHelper.applyTrim(position); }
    public void setPosition(String position) { this.position = TextHelper.applyTrim(position); }

    public String getArea() { return TextHelper.applyTrim(area); }
    public void setArea(String area) { this.area = TextHelper.applyTrim(area); }

    public String getCostCenter() { return TextHelper.applyTrim(costCenter); }
    public void setCostCenter(String costCenter) { this.costCenter = TextHelper.applyTrim(costCenter); }

    public String getContractType() { return TextHelper.applyTrim(contractType); }
    public void setContractType(String contractType) { this.contractType = TextHelper.applyTrim(contractType); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = ObjectHelper.getDefault(createdAt, LocalDateTime.now()); }

    @Override
    public boolean isNew() { return newEntity; }

    public void markNew() { this.newEntity = true; }

    public void markPersisted() { this.newEntity = false; }
}
