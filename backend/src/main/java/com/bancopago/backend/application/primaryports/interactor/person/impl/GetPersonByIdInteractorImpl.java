package com.bancopago.backend.application.primaryports.interactor.person.impl;

import com.bancopago.backend.application.primaryports.dto.person.response.GetPersonByIdResponse;
import com.bancopago.backend.application.primaryports.interactor.person.GetPersonByIdInteractor;
import com.bancopago.backend.application.primaryports.mapper.person.PersonDTOMapper;
import com.bancopago.backend.application.usecase.person.GetPersonByIdUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class GetPersonByIdInteractorImpl implements GetPersonByIdInteractor {

    private final GetPersonByIdUseCase getPersonByIdUseCase;
    private final PersonDTOMapper personDTOMapper;

    public GetPersonByIdInteractorImpl(GetPersonByIdUseCase getPersonByIdUseCase,
                                       PersonDTOMapper personDTOMapper) {
        this.getPersonByIdUseCase = getPersonByIdUseCase;
        this.personDTOMapper = personDTOMapper;
    }

    @Override
    public Mono<GetPersonByIdResponse> execute(UUID personId) {
        return getPersonByIdUseCase.execute(personId)
                .map(personDTOMapper::toGetPersonByIdResponse);
    }
}
