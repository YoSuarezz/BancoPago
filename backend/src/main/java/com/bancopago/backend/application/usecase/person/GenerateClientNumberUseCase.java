package com.bancopago.backend.application.usecase.person;

import com.bancopago.backend.application.usecase.UseCaseWithReturn;
import com.bancopago.backend.domain.enums.PersonType;

public interface GenerateClientNumberUseCase extends UseCaseWithReturn<PersonType, String> {
}
