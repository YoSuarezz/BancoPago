package com.bancopago.backend.application.primaryports.interactor.person;

import com.bancopago.backend.application.model.PageResult;
import com.bancopago.backend.application.primaryports.dto.person.request.PersonPageRequest;
import com.bancopago.backend.application.primaryports.dto.person.response.ListPersonResponse;
import com.bancopago.backend.application.primaryports.interactor.InteractorWithReturn;

public interface ListPersonsPagedInteractor extends InteractorWithReturn<PersonPageRequest, PageResult<ListPersonResponse>> {
}
