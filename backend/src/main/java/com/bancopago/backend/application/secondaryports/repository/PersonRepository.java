package com.bancopago.backend.application.secondaryports.repository;

import com.bancopago.backend.domain.person.PersonDomain;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PersonRepository {
    Mono<PersonDomain> save(PersonDomain person);
    Mono<PersonDomain> findById(UUID id);
    Mono<PersonDomain> findByDocument(String document, String documentType);
    Mono<Boolean> existsByDocument(String document);
}
