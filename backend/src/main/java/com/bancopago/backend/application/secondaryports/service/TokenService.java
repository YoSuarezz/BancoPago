package com.bancopago.backend.application.secondaryports.service;

import com.bancopago.backend.domain.auth.UserDomain;

public interface TokenService {
    String generateToken(UserDomain user);
}
