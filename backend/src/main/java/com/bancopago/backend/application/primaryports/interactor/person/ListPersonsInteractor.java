package com.bancopago.backend.application.primaryports.interactor.person;

import com.bancopago.backend.application.primaryports.dto.person.response.ListPersonResponse;
import com.bancopago.backend.application.primaryports.interactor.InteractorWithoutInput;

import java.util.List;

public interface ListPersonsInteractor extends InteractorWithoutInput<List<ListPersonResponse>> {
}
