package com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.person;

import com.bancopago.backend.application.secondaryports.entity.PersonEntity;
import com.bancopago.backend.application.secondaryports.mapper.PersonEntityMapper;
import com.bancopago.backend.application.secondaryports.repository.PersonRepository;
import com.bancopago.backend.domain.person.PersonDomain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class PersonR2dbcAdapter implements PersonRepository {

    private final PersonR2dbcRepository personR2dbcRepository;
    private final PersonEntityMapper personEntityMapper;

    public PersonR2dbcAdapter(PersonR2dbcRepository personR2dbcRepository,
                              PersonEntityMapper personEntityMapper) {
        this.personR2dbcRepository = personR2dbcRepository;
        this.personEntityMapper = personEntityMapper;
    }

    @Override
    public Mono<PersonDomain> savePerson(PersonDomain person) {
        return personR2dbcRepository.findById(person.getId())
                .flatMap(existing -> {
                    PersonEntity entity = personEntityMapper.toEntity(person);
                    entity.setCreatedAt(existing.getCreatedAt());
                    entity.markPersisted();
                    return personR2dbcRepository.save(entity);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    PersonEntity entity = personEntityMapper.toEntity(person);
                    entity.markNew();
                    return personR2dbcRepository.save(entity);
                }))
                .map(personEntityMapper::toDomain);
    }

    @Override
    public Mono<PersonDomain> findPersonById(UUID personId) {
        return personR2dbcRepository.findById(personId).map(personEntityMapper::toDomain);
    }

    @Override
    public Mono<PersonDomain> findPersonByDocument(String documentNumber, String documentType) {
        return personR2dbcRepository.findByDocumentNumberAndDocumentType(documentNumber, documentType)
                .map(personEntityMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsPersonByDocument(String documentNumber) {
        return personR2dbcRepository.existsByDocumentNumber(documentNumber);
    }
}
