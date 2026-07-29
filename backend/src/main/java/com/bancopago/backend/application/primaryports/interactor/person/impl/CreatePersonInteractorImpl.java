package com.bancopago.backend.application.primaryports.interactor.person.impl;

import com.bancopago.backend.application.primaryports.dto.person.request.CreatePersonRequest;
import com.bancopago.backend.application.primaryports.dto.person.response.CreatePersonResponse;
import com.bancopago.backend.application.primaryports.interactor.person.CreatePersonInteractor;
import com.bancopago.backend.application.primaryports.mapper.person.PersonDTOMapper;
import com.bancopago.backend.application.usecase.person.CreatePersonUseCase;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CreatePersonInteractorImpl implements CreatePersonInteractor {

    private final CreatePersonUseCase createPersonUseCase;
    private final PersonDTOMapper personDTOMapper;

    public CreatePersonInteractorImpl(CreatePersonUseCase createPersonUseCase,
                                      PersonDTOMapper personDTOMapper) {
        this.createPersonUseCase = createPersonUseCase;
        this.personDTOMapper = personDTOMapper;
    }

    @Override
    public Mono<CreatePersonResponse> createPerson(CreatePersonRequest request) {
        var domain = personDTOMapper.toDomain(request);
        return createPersonUseCase.createPerson(domain)
                .map(personDTOMapper::toCreatePersonResponse);
    }
}
