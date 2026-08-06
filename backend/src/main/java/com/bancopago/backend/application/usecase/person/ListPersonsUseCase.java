package com.bancopago.backend.application.usecase.person;

import com.bancopago.backend.application.usecase.UseCaseWithoutInput;
import com.bancopago.backend.domain.person.PersonDomain;

import java.util.List;

public interface ListPersonsUseCase extends UseCaseWithoutInput<List<PersonDomain>> {
}
