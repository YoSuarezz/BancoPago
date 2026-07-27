package com.bancopago.backend.infrastructure.secondaryadapters.r2dbc;

import com.bancopago.backend.application.secondaryports.entity.PersonEntity;
import com.bancopago.backend.application.secondaryports.mapper.PersonEntityMapper;
import com.bancopago.backend.application.secondaryports.repository.PersonRepository;
import com.bancopago.backend.domain.person.PersonDomain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class PersonR2dbcAdapter implements PersonRepository {

    private final PersonR2dbcRepository repository;
    private final PersonEntityMapper mapper;

    public PersonR2dbcAdapter(PersonR2dbcRepository repository, PersonEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<PersonDomain> save(PersonDomain person) {
        return repository.findById(person.getId())
                .flatMap(existing -> {
                    PersonEntity entity = mapper.toEntity(person);
                    entity.setCreatedAt(existing.getCreatedAt());
                    entity.markPersisted();
                    return repository.save(entity);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    PersonEntity entity = mapper.toEntity(person);
                    entity.markNew();
                    return repository.save(entity);
                }))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<PersonDomain> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<PersonDomain> findByDocument(String document, String documentType) {
        return repository.findByDocumentoAndTipoDocumento(document, documentType)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByDocument(String document) {
        return repository.existsByDocumento(document);
    }
}
