package com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.user;

import com.bancopago.backend.application.secondaryports.repository.UserRepository;
import com.bancopago.backend.domain.auth.UserDomain;
import com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.mapper.UserEntityMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class UserR2dbcAdapter implements UserRepository {

    private final UserR2dbcRepository r2dbcRepository;
    private final UserEntityMapper mapper;

    public UserR2dbcAdapter(UserR2dbcRepository r2dbcRepository, UserEntityMapper mapper) {
        this.r2dbcRepository = r2dbcRepository;
        this.mapper = mapper;
    }

    @Override
    public Mono<UserDomain> saveUser(UserDomain user) {
        var entity = mapper.toUserEntity(user);
        return r2dbcRepository.save(entity).map(mapper::toUserDomain);
    }

    @Override
    public Mono<UserDomain> findUserByEmail(String email) {
        return r2dbcRepository.findByEmail(email).map(mapper::toUserDomain);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return r2dbcRepository.existsByEmail(email);
    }
}
