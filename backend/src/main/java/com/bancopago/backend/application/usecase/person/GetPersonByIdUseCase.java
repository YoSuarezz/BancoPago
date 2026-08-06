package com.bancopago.backend.application.usecase.person;

import com.bancopago.backend.application.usecase.UseCaseWithReturn;
import com.bancopago.backend.domain.person.PersonDomain;

import java.util.UUID;

public interface GetPersonByIdUseCase extends UseCaseWithReturn<UUID, PersonDomain> {
}
