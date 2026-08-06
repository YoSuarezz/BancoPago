package com.bancopago.backend.application.primaryports.interactor.person.impl;

import com.bancopago.backend.application.primaryports.dto.person.request.CreatePersonRequest;
import com.bancopago.backend.application.primaryports.dto.person.response.CreatePersonResponse;
import com.bancopago.backend.application.primaryports.interactor.person.CreatePersonInteractor;
import com.bancopago.backend.application.primaryports.mapper.person.PersonDTOMapper;
import com.bancopago.backend.application.usecase.person.CreatePersonUseCase;
import com.bancopago.backend.application.usecase.person.GenerateClientNumberUseCase;
import com.bancopago.backend.domain.enums.PersonType;
import com.bancopago.backend.domain.person.PersonDomain;
import com.bancopago.backend.domain.person.PersonError;
import com.bancopago.backend.domain.person.exceptions.InvalidPersonException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CreatePersonInteractorImpl implements CreatePersonInteractor {

    private final CreatePersonUseCase createPersonUseCase;
    private final GenerateClientNumberUseCase generateClientNumberUseCase;
    private final PersonDTOMapper personDTOMapper;

    public CreatePersonInteractorImpl(CreatePersonUseCase createPersonUseCase,
                                      GenerateClientNumberUseCase generateClientNumberUseCase,
                                      PersonDTOMapper personDTOMapper) {
        this.createPersonUseCase = createPersonUseCase;
        this.generateClientNumberUseCase = generateClientNumberUseCase;
        this.personDTOMapper = personDTOMapper;
    }

    @Override
    public Mono<CreatePersonResponse> execute(CreatePersonRequest request) {
        return buildDomain(request)
                .flatMap(createPersonUseCase::execute)
                .map(personDTOMapper::toCreatePersonResponse);
    }

    private Mono<PersonDomain> buildDomain(CreatePersonRequest request) {
        if (request.personType() == PersonType.CLIENT) {
            return generateClientNumberUseCase.execute(PersonType.CLIENT)
                    .map(clientNumber -> personDTOMapper.toClientDomain(request, clientNumber));
        }
        if (request.personType() == PersonType.EMPLOYEE) {
            return Mono.fromCallable(() -> personDTOMapper.toEmployeeDomain(request));
        }
        return Mono.error(InvalidPersonException.create(PersonError.TYPE_REQUIRED));
    }
}
