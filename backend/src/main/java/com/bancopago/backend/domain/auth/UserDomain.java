package com.bancopago.backend.domain.auth;

import com.bancopago.backend.crosscutting.helpers.TextHelper;
import com.bancopago.backend.domain.BaseDomain;
import com.bancopago.backend.domain.auth.exceptions.InvalidUserException;
import com.bancopago.backend.domain.enums.UserRole;

import java.util.UUID;

public class UserDomain extends BaseDomain {

    private final String email;
    private String passwordHash;
    private final UserRole role;
    private final UUID personId;

    public UserDomain(UUID id, String email, String passwordHash, UserRole role, UUID personId) {
        super(id);
        if (TextHelper.isBlank(email)) throw InvalidUserException.create(UserError.EMAIL_REQUIRED);
        if (TextHelper.isBlank(passwordHash)) throw InvalidUserException.create(UserError.PASSWORD_REQUIRED);
        if (role == null) throw InvalidUserException.create(UserError.ROLE_REQUIRED);
        this.email = email.trim().toLowerCase();
        this.passwordHash = passwordHash;
        this.role = role;
        this.personId = personId;
    }

    public static UserDomain create(String email, String passwordHash, UserRole role, UUID personId) {
        return new UserDomain(UUID.randomUUID(), email, passwordHash, role, personId);
    }

    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public UUID getPersonId() { return personId; }

    public void updatePasswordHash(String newHash) {
        if (TextHelper.isBlank(newHash)) throw InvalidUserException.create(UserError.PASSWORD_REQUIRED);
        this.passwordHash = newHash;
    }
}
