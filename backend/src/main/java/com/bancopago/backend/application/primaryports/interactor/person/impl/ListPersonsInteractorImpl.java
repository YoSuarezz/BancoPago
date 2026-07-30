package com.bancopago.backend.application.primaryports.interactor.person.impl;

import com.bancopago.backend.application.primaryports.dto.person.response.ListPersonResponse;
import com.bancopago.backend.application.primaryports.interactor.person.ListPersonsInteractor;
import com.bancopago.backend.application.primaryports.mapper.person.PersonDTOMapper;
import com.bancopago.backend.application.usecase.person.ListPersonsUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ListPersonsInteractorImpl implements ListPersonsInteractor {

    private final ListPersonsUseCase listPersonsUseCase;
    private final PersonDTOMapper personDTOMapper;

    public ListPersonsInteractorImpl(ListPersonsUseCase listPersonsUseCase,
                                     PersonDTOMapper personDTOMapper) {
        this.listPersonsUseCase = listPersonsUseCase;
        this.personDTOMapper = personDTOMapper;
    }

    @Override
    public Mono<List<ListPersonResponse>> execute() {
        return listPersonsUseCase.execute()
                .map(persons -> persons.stream().map(personDTOMapper::toListPersonResponse).toList());
    }
}
