package com.bancopago.backend.infrastructure.primaryadapters.controller.person;

import com.bancopago.backend.application.model.PageResult;
import com.bancopago.backend.application.primaryports.dto.person.request.CreatePersonRequest;
import com.bancopago.backend.application.primaryports.dto.person.request.PersonPageRequest;
import com.bancopago.backend.application.primaryports.dto.person.response.CreateClientResponse;
import com.bancopago.backend.application.primaryports.dto.person.response.CreateEmployeeResponse;
import com.bancopago.backend.application.primaryports.dto.person.response.GetClientByIdResponse;
import com.bancopago.backend.application.primaryports.dto.person.response.ListPersonResponse;
import com.bancopago.backend.application.primaryports.interactor.person.CreatePersonInteractor;
import com.bancopago.backend.application.primaryports.interactor.person.GetPersonByIdInteractor;
import com.bancopago.backend.application.primaryports.interactor.person.ListPersonsInteractor;
import com.bancopago.backend.application.primaryports.interactor.person.ListPersonsPagedInteractor;
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
import java.util.List;
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

    @MockitoBean
    private ListPersonsInteractor listPersonsInteractor;

    @MockitoBean
    private ListPersonsPagedInteractor listPersonsPagedInteractor;

    @Test
    void createClient_returns201() {
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
    void createEmployee_returns201() {
        UUID id = UUID.randomUUID();
        when(createPersonInteractor.execute(any(CreatePersonRequest.class)))
                .thenReturn(Mono.just(new CreateEmployeeResponse(
                        id, "Carlos", "456", "CC", "carlos@test.com", null,
                        "EMPLOYEE", "Dev", "Engineering", "CC-1", "PERMANENT")));

        webTestClient.mutateWith(csrf()).post()
                .uri("/api/v1/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Carlos",
                          "documentNumber": "456",
                          "documentType": "CC",
                          "email": "carlos@test.com",
                          "personType": "EMPLOYEE",
                          "position": "Dev",
                          "area": "Engineering",
                          "costCenter": "CC-1",
                          "contractType": "PERMANENT"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data[0].id").isEqualTo(id.toString())
                .jsonPath("$.data[0].position").isEqualTo("Dev")
                .jsonPath("$.data[0].clientNumber").doesNotExist();
    }

    @Test
    void createPerson_returns400WhenNameMissing() {
        webTestClient.mutateWith(csrf()).post()
                .uri("/api/v1/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "documentNumber": "123",
                          "documentType": "CC",
                          "email": "ana@test.com",
                          "personType": "CLIENT"
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest();
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

    @Test
    void searchPersons_returnsFlatPageResponse() {
        UUID id = UUID.randomUUID();
        when(listPersonsPagedInteractor.execute(any(PersonPageRequest.class)))
                .thenReturn(Mono.just(new PageResult<>(
                        List.of(new ListPersonResponse(id, "Ana", "123", "CC", "CLIENT")),
                        1,
                        0,
                        10
                )));

        webTestClient.get()
                .uri("/api/v1/persons/search?page=0&size=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].id").isEqualTo(id.toString())
                .jsonPath("$.content[0].name").isEqualTo("Ana")
                .jsonPath("$.page").isEqualTo(0)
                .jsonPath("$.size").isEqualTo(10)
                .jsonPath("$.totalElements").isEqualTo(1)
                .jsonPath("$.totalPages").isEqualTo(1)
                .jsonPath("$.data").doesNotExist()
                .jsonPath("$.messages").doesNotExist();
    }
}
