package com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.mapper;

import com.bancopago.backend.domain.auth.UserDomain;
import com.bancopago.backend.domain.enums.UserRole;
import com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {

    public UserEntity toUserEntity(UserDomain domain) {
        if (domain == null) return null;
        UserEntity entity = new UserEntity();
        entity.setId(domain.getId());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setRole(domain.getRole().name());
        entity.setPersonId(domain.getPersonId());
        entity.markNew();
        return entity;
    }

    public UserDomain toUserDomain(UserEntity entity) {
        if (entity == null) return null;
        return new UserDomain(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                UserRole.valueOf(entity.getRole()),
                entity.getPersonId()
        );
    }
}
