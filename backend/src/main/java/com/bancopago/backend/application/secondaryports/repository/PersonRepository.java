package com.bancopago.backend.application.secondaryports.repository;

import com.bancopago.backend.application.model.PageResult;
import com.bancopago.backend.application.model.PersonQuery;
import com.bancopago.backend.domain.person.PersonDomain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PersonRepository {

    Mono<PersonDomain> savePerson(PersonDomain person);

    Mono<PersonDomain> findPersonById(UUID personId);

    Flux<PersonDomain> findAllPersons();

    Mono<PageResult<PersonDomain>> findPersonsPage(PersonQuery request);

    Mono<PersonDomain> findPersonByDocument(String documentNumber, String documentType);

    Mono<Boolean> existsPersonByDocument(String documentNumber);

    Mono<Boolean> existsPersonByDocument(String documentNumber, String documentType);

    Mono<Boolean> existsPersonByEmail(String email);
}
