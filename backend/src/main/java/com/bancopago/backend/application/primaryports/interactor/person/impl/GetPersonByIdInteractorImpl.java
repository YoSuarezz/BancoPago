package com.bancopago.backend.application.primaryports.interactor.person.impl;

import com.bancopago.backend.application.primaryports.dto.person.response.GetPersonByIdResponse;
import com.bancopago.backend.application.primaryports.interactor.person.GetPersonByIdInteractor;
import com.bancopago.backend.application.primaryports.mapper.person.PersonDTOMapper;
import com.bancopago.backend.application.usecase.person.GetPersonByIdUseCase;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class GetPersonByIdInteractorImpl implements GetPersonByIdInteractor {

    private final GetPersonByIdUseCase getPersonByIdUseCase;
    private final PersonDTOMapper personDTOMapper;

    public GetPersonByIdInteractorImpl(GetPersonByIdUseCase getPersonByIdUseCase,
                                       PersonDTOMapper personDTOMapper) {
        this.getPersonByIdUseCase = getPersonByIdUseCase;
        this.personDTOMapper = personDTOMapper;
    }

    @Override
    public Mono<GetPersonByIdResponse> getPersonById(UUID personId) {
        return getPersonByIdUseCase.getPersonById(personId)
                .map(personDTOMapper::toGetPersonByIdResponse);
    }
}
