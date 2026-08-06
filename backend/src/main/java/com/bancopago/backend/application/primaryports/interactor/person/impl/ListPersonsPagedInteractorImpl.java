package com.bancopago.backend.application.primaryports.interactor.person.impl;

import com.bancopago.backend.application.model.PageResult;
import com.bancopago.backend.application.model.PersonQuery;
import com.bancopago.backend.application.primaryports.dto.person.request.PersonPageRequest;
import com.bancopago.backend.application.primaryports.dto.person.response.ListPersonResponse;
import com.bancopago.backend.application.primaryports.interactor.person.ListPersonsPagedInteractor;
import com.bancopago.backend.application.primaryports.mapper.person.PersonDTOMapper;
import com.bancopago.backend.application.usecase.person.ListPersonsPagedUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ListPersonsPagedInteractorImpl implements ListPersonsPagedInteractor {

    private final ListPersonsPagedUseCase listPersonsPagedUseCase;
    private final PersonDTOMapper personDTOMapper;

    public ListPersonsPagedInteractorImpl(ListPersonsPagedUseCase listPersonsPagedUseCase,
                                          PersonDTOMapper personDTOMapper) {
        this.listPersonsPagedUseCase = listPersonsPagedUseCase;
        this.personDTOMapper = personDTOMapper;
    }

    @Override
    public Mono<PageResult<ListPersonResponse>> execute(PersonPageRequest request) {
        return listPersonsPagedUseCase.execute(toQuery(request))
                .map(pageResult -> pageResult.map(personDTOMapper::toListPersonResponse));
    }

    private static PersonQuery toQuery(PersonPageRequest request) {
        return new PersonQuery(
                request.page(),
                request.size(),
                request.sortBy(),
                request.sortDirection(),
                request.name(),
                request.personType()
        );
    }
}
