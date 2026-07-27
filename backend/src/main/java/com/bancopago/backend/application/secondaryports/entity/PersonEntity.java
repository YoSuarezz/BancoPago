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

@Table("persona")
public class PersonEntity implements Persistable<UUID> {

    @Id
    private UUID id;
    private String nombre;
    private String documento;
    @Column("tipo_documento")
    private String tipoDocumento;
    private String email;
    private String telefono;
    private String tipo;
    @Column("created_at")
    private LocalDateTime createdAt;

    @Transient
    private boolean newEntity = true;

    public PersonEntity() {
        setId(UUID.randomUUID());
        setNombre(TextHelper.EMPTY);
        setDocumento(TextHelper.EMPTY);
        setTipoDocumento(TextHelper.EMPTY);
        setEmail(TextHelper.EMPTY);
        setTelefono(TextHelper.EMPTY);
        setTipo(TextHelper.EMPTY);
        setCreatedAt(LocalDateTime.now());
        this.newEntity = true;
    }

    public static PersonEntity create() { return new PersonEntity(); }

    public static PersonEntity create(UUID id, String nombre, String documento,
                                       String tipoDocumento, String email,
                                       String telefono, String tipo, LocalDateTime createdAt) {
        PersonEntity entity = new PersonEntity();
        entity.setId(id);
        entity.setNombre(nombre);
        entity.setDocumento(documento);
        entity.setTipoDocumento(tipoDocumento);
        entity.setEmail(email);
        entity.setTelefono(telefono);
        entity.setTipo(tipo);
        entity.setCreatedAt(createdAt);
        return entity;
    }

    public static PersonEntity create(String nombre, String documento,
                                       String tipoDocumento, String email,
                                       String telefono, String tipo) {
        return create(UUID.randomUUID(), nombre, documento, tipoDocumento,
                email, telefono, tipo, LocalDateTime.now());
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = ObjectHelper.getDefault(id, UUID.randomUUID()); }

    public String getNombre() { return TextHelper.applyTrim(nombre); }
    public void setNombre(String nombre) { this.nombre = TextHelper.applyTrim(nombre); }

    public String getDocumento() { return TextHelper.applyTrim(documento); }
    public void setDocumento(String documento) { this.documento = TextHelper.applyTrim(documento); }

    public String getTipoDocumento() { return TextHelper.applyTrim(tipoDocumento); }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = TextHelper.applyTrim(tipoDocumento); }

    public String getEmail() { return TextHelper.applyTrim(email); }
    public void setEmail(String email) { this.email = TextHelper.applyTrim(email); }

    public String getTelefono() { return TextHelper.applyTrim(telefono); }
    public void setTelefono(String telefono) { this.telefono = TextHelper.applyTrim(telefono); }

    public String getTipo() { return TextHelper.applyTrim(tipo); }
    public void setTipo(String tipo) { this.tipo = TextHelper.applyTrim(tipo); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = ObjectHelper.getDefault(createdAt, LocalDateTime.now()); }

    @Override
    public boolean isNew() { return newEntity; }

    public void markNew() { this.newEntity = true; }

    public void markPersisted() { this.newEntity = false; }
}
