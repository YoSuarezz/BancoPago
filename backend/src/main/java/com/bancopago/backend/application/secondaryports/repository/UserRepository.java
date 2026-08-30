package com.bancopago.backend.application.secondaryports.repository;

import com.bancopago.backend.domain.auth.UserDomain;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<UserDomain> saveUser(UserDomain user);
    Mono<UserDomain> findUserByEmail(String email);
    Mono<Boolean> existsByEmail(String email);
}
