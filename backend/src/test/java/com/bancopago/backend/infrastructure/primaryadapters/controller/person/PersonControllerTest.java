package com.bancopago.backend.infrastructure.primaryadapters.controller.person;

import com.bancopago.backend.application.primaryports.dto.person.request.CreatePersonRequest;
import com.bancopago.backend.application.primaryports.dto.person.response.CreateClientResponse;
import com.bancopago.backend.application.primaryports.dto.person.response.GetClientByIdResponse;
import com.bancopago.backend.application.primaryports.interactor.person.CreatePersonInteractor;
import com.bancopago.backend.application.primaryports.interactor.person.GetPersonByIdInteractor;
import com.bancopago.backend.domain.person.exceptions.PersonNotFoundException;
import com.bancopago.backend.infrastructure.GlobalExceptionHandler;
import com.bancopago.backend.infrastructure.ResponseMessages;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = PersonController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser
class PersonControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CreatePersonInteractor createPersonInteractor;

    @MockitoBean
    private GetPersonByIdInteractor getPersonByIdInteractor;

    @Test
    void createPerson_returns201() {
        UUID id = UUID.randomUUID();
        when(createPersonInteractor.execute(any(CreatePersonRequest.class)))
                .thenReturn(Mono.just(new CreateClientResponse(
                        id, "Ana", "123", "CC", "ana@test.com", null,
                        "CLIENT", "C-1", LocalDate.now())));

        webTestClient.mutateWith(csrf()).post()
                .uri("/api/v1/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Ana",
                          "documentNumber": "123",
                          "documentType": "CC",
                          "email": "ana@test.com",
                          "personType": "CLIENT"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data[0].id").isEqualTo(id.toString())
                .jsonPath("$.data[0].name").isEqualTo("Ana")
                .jsonPath("$.messages[0]").isEqualTo(ResponseMessages.PERSON_CREATED)
                .jsonPath("$.data[0].position").doesNotExist();
    }

    @Test
    void getPerson_returns200() {
        UUID id = UUID.randomUUID();
        when(getPersonByIdInteractor.execute(id))
                .thenReturn(Mono.just(new GetClientByIdResponse(
                        id, "Ana", "123", "CC", "ana@test.com", null,
                        "CLIENT", "C-1", LocalDate.now())));

        webTestClient.get()
                .uri("/api/v1/persons/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].id").isEqualTo(id.toString())
                .jsonPath("$.data[0].position").doesNotExist();
    }

    @Test
    void getPerson_returns404WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(getPersonByIdInteractor.execute(id))
                .thenReturn(Mono.error(PersonNotFoundException.create(id)));

        webTestClient.get()
                .uri("/api/v1/persons/{id}", id)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("PERSON_NOT_FOUND")
                .jsonPath("$.message").isEqualTo("No se encontró la persona con id " + id);
    }
}
