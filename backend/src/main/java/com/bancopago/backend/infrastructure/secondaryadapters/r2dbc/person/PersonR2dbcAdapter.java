package com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.person;

import com.bancopago.backend.application.model.PageResult;
import com.bancopago.backend.application.model.PersonQuery;
import com.bancopago.backend.application.secondaryports.repository.PersonRepository;
import com.bancopago.backend.crosscutting.helpers.TextHelper;
import com.bancopago.backend.domain.person.PersonDomain;
import com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.entity.PersonEntity;
import com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.mapper.PersonEntityMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
public class PersonR2dbcAdapter implements PersonRepository {

    private final PersonR2dbcRepository personR2dbcRepository;
    private final PersonEntityMapper personEntityMapper;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    public PersonR2dbcAdapter(PersonR2dbcRepository personR2dbcRepository,
                              PersonEntityMapper personEntityMapper,
                              R2dbcEntityTemplate r2dbcEntityTemplate) {
        this.personR2dbcRepository = personR2dbcRepository;
        this.personEntityMapper = personEntityMapper;
        this.r2dbcEntityTemplate = r2dbcEntityTemplate;
    }

    @Override
    public Mono<PersonDomain> savePerson(PersonDomain person) {
        return personR2dbcRepository.findById(person.getId())
                .flatMap(existing -> {
                    PersonEntity entity = personEntityMapper.toPersonEntity(person);
                    entity.setCreatedAt(existing.getCreatedAt());
                    entity.markPersisted();
                    return personR2dbcRepository.save(entity);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    PersonEntity entity = personEntityMapper.toPersonEntity(person);
                    entity.markNew();
                    return personR2dbcRepository.save(entity);
                }))
                .map(personEntityMapper::toPersonDomain);
    }

    @Override
    public Mono<PersonDomain> findPersonById(UUID personId) {
        return personR2dbcRepository.findById(personId).map(personEntityMapper::toPersonDomain);
    }

    @Override
    public Flux<PersonDomain> findAllPersons() {
        return personR2dbcRepository.findAll().map(personEntityMapper::toPersonDomain);
    }

    @Override
    public Mono<PageResult<PersonDomain>> findPersonsPage(PersonQuery request) {
        Criteria criteria = buildCriteria(request);
        Sort sort = Sort.by(resolveDirection(request.sortDirection()), resolveSortBy(request.sortBy()));

        Query pageQuery = Query.query(criteria)
                .sort(sort)
                .offset((long) request.page() * request.size())
                .limit(request.size());

        Flux<PersonDomain> rows = r2dbcEntityTemplate.select(PersonEntity.class)
                .matching(pageQuery)
                .all()
                .map(personEntityMapper::toPersonDomain);

        Mono<Long> total = r2dbcEntityTemplate.select(PersonEntity.class)
                .matching(Query.query(criteria))
                .count();

        return Mono.zip(rows.collectList(), total)
                .map(tuple -> new PageResult<>(tuple.getT1(), tuple.getT2(), request.page(), request.size()));
    }

    @Override
    public Mono<PersonDomain> findPersonByDocument(String documentNumber, String documentType) {
        return personR2dbcRepository.findByDocumentNumberAndDocumentType(documentNumber, documentType)
                .map(personEntityMapper::toPersonDomain);
    }

    @Override
    public Mono<Boolean> existsPersonByDocument(String documentNumber) {
        return personR2dbcRepository.existsByDocumentNumber(documentNumber);
    }

    @Override
    public Mono<Boolean> existsPersonByDocument(String documentNumber, String documentType) {
        return personR2dbcRepository.existsByDocumentNumberAndDocumentType(documentNumber, documentType);
    }

    @Override
    public Mono<Boolean> existsPersonByEmail(String email) {
        return personR2dbcRepository.existsByEmailIgnoreCase(email);
    }

    private static Criteria buildCriteria(PersonQuery request) {
        List<Criteria> filters = new java.util.ArrayList<>();

        if (!TextHelper.isBlank(request.name())) {
            filters.add(Criteria.where("name").like("%" + escapeLike(request.name().trim()) + "%"));
        }
        if (request.personType() != null) {
            filters.add(Criteria.where("person_type").is(request.personType().name()));
        }

        if (filters.isEmpty()) {
            return Criteria.empty();
        }

        Criteria criteria = filters.getFirst();
        for (int i = 1; i < filters.size(); i++) {
            criteria = criteria.and(filters.get(i));
        }
        return criteria;
    }

    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private static Sort.Direction resolveDirection(String sortDirection) {
        try {
            return Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException ignored) {
            return Sort.Direction.ASC;
        }
    }

    private static String resolveSortBy(String sortBy) {
        return switch (sortBy) {
            case "documentNumber" -> "document_number";
            case "personType" -> "person_type";
            case "createdAt" -> "created_at";
            default -> "name";
        };
    }
}
