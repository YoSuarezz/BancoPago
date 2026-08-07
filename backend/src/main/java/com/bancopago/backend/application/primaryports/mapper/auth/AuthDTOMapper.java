package com.bancopago.backend.application.primaryports.mapper.auth;

import com.bancopago.backend.application.primaryports.dto.auth.request.LoginRequest;
import com.bancopago.backend.application.primaryports.dto.auth.request.RegisterRequest;
import com.bancopago.backend.application.primaryports.dto.auth.response.AuthResponse;
import com.bancopago.backend.domain.auth.LoginCredential;
import com.bancopago.backend.domain.auth.UserDomain;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class AuthDTOMapper {

    public UserDomain toUserDomain(RegisterRequest request) {
        return UserDomain.create(
                request.email(),
                request.password(),
                request.role(),
                null
        );
    }

    public LoginCredential toLoginCredential(LoginRequest request) {
        return new LoginCredential(request.email(), request.password());
    }

    public AuthResponse toAuthResponse(UserDomain user, String token) {
        return AuthResponse.of(token, user.getEmail(), user.getRole());
    }
}
