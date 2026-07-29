package com.bancopago.backend.application.primaryports.interactor.person.impl;

import com.bancopago.backend.application.primaryports.dto.person.request.CreatePersonRequest;
import com.bancopago.backend.application.primaryports.dto.person.response.CreatePersonResponse;
import com.bancopago.backend.application.primaryports.interactor.person.CreatePersonInteractor;
import com.bancopago.backend.application.primaryports.mapper.person.PersonDTOMapper;
import com.bancopago.backend.application.usecase.person.CreatePersonUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CreatePersonInteractorImpl implements CreatePersonInteractor {

    private final CreatePersonUseCase createPersonUseCase;
    private final PersonDTOMapper personDTOMapper;

    public CreatePersonInteractorImpl(CreatePersonUseCase createPersonUseCase,
                                      PersonDTOMapper personDTOMapper) {
        this.createPersonUseCase = createPersonUseCase;
        this.personDTOMapper = personDTOMapper;
    }

    @Override
    public Mono<CreatePersonResponse> execute(CreatePersonRequest request) {
        var domain = personDTOMapper.toPersonDomain(request);
        return createPersonUseCase.execute(domain)
                .map(personDTOMapper::toCreatePersonResponse);
    }
}
