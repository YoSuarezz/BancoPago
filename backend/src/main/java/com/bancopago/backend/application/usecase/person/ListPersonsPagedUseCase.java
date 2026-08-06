package com.bancopago.backend.application.usecase.person;

import com.bancopago.backend.application.model.PageResult;
import com.bancopago.backend.application.model.PersonQuery;
import com.bancopago.backend.application.usecase.UseCaseWithReturn;
import com.bancopago.backend.domain.person.PersonDomain;

public interface ListPersonsPagedUseCase extends UseCaseWithReturn<PersonQuery, PageResult<PersonDomain>> {
}
