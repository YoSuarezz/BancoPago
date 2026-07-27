package com.bancopago.backend.application.secondaryports.repository;

import com.bancopago.backend.domain.person.PersonDomain;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PersonRepository {

    Mono<PersonDomain> savePerson(PersonDomain person);

    Mono<PersonDomain> findPersonById(UUID personId);

    Mono<PersonDomain> findPersonByDocument(String documentNumber, String documentType);

    Mono<Boolean> existsPersonByDocument(String documentNumber);
}
