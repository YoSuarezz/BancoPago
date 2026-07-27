package com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.person;

import com.bancopago.backend.application.secondaryports.entity.PersonEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface PersonR2dbcRepository extends R2dbcRepository<PersonEntity, UUID> {

    Mono<PersonEntity> findByDocumentNumberAndDocumentType(String documentNumber, String documentType);

    Mono<Boolean> existsByDocumentNumber(String documentNumber);
}
