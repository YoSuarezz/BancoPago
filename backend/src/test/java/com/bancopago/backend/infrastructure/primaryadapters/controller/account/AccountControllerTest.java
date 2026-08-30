package com.bancopago.backend.infrastructure.primaryadapters.controller.account;

import com.bancopago.backend.application.primaryports.dto.account.request.CreateAccountRequest;
import com.bancopago.backend.application.primaryports.dto.account.response.AccountStatusResponse;
import com.bancopago.backend.application.primaryports.dto.account.response.CreateAccountResponse;
import com.bancopago.backend.application.primaryports.dto.account.response.GetAccountBalanceResponse;
import com.bancopago.backend.application.primaryports.dto.account.response.ListAccountResponse;
import com.bancopago.backend.application.primaryports.interactor.account.BlockAccountInteractor;
import com.bancopago.backend.application.primaryports.interactor.account.CloseAccountInteractor;
import com.bancopago.backend.application.primaryports.interactor.account.CreateAccountInteractor;
import com.bancopago.backend.application.primaryports.interactor.account.GetAccountBalanceInteractor;
import com.bancopago.backend.application.primaryports.interactor.account.ListAccountsByOwnerInteractor;
import com.bancopago.backend.application.primaryports.interactor.account.StreamAccountBalanceInteractor;
import com.bancopago.backend.application.primaryports.interactor.account.UnblockAccountInteractor;
import com.bancopago.backend.domain.account.exceptions.AccountNotFoundException;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = AccountController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser
class AccountControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CreateAccountInteractor createAccountInteractor;

    @MockitoBean
    private GetAccountBalanceInteractor getAccountBalanceInteractor;

    @MockitoBean
    private StreamAccountBalanceInteractor streamAccountBalanceInteractor;

    @MockitoBean
    private ListAccountsByOwnerInteractor listAccountsByOwnerInteractor;

    @MockitoBean
    private BlockAccountInteractor blockAccountInteractor;

    @MockitoBean
    private UnblockAccountInteractor unblockAccountInteractor;

    @MockitoBean
    private CloseAccountInteractor closeAccountInteractor;

    @Test
    void createAccount_returns201() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(createAccountInteractor.execute(any(CreateAccountRequest.class)))
                .thenReturn(Mono.just(new CreateAccountResponse(
                        id, ownerId, "5300000001", "SAVINGS",
                        BigDecimal.ZERO, "COP", "ACTIVE")));

        webTestClient.mutateWith(csrf()).post()
                .uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "ownerId": "%s",
                          "type": "SAVINGS"
                        }
                        """.formatted(ownerId))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data[0].id").isEqualTo(id.toString())
                .jsonPath("$.data[0].number").isEqualTo("5300000001");
    }

    @Test
    void listAccountsByOwner_returns200() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(listAccountsByOwnerInteractor.execute(ownerId))
                .thenReturn(Mono.just(List.of(new ListAccountResponse(
                        id, ownerId, "5300000001", "SAVINGS",
                        BigDecimal.ZERO, "COP", "ACTIVE"))));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/accounts")
                        .queryParam("ownerId", ownerId)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].id").isEqualTo(id.toString())
                .jsonPath("$.data[0].ownerId").isEqualTo(ownerId.toString());
    }

    @Test
    void getBalance_returns200() {
        UUID id = UUID.randomUUID();
        when(getAccountBalanceInteractor.execute(id))
                .thenReturn(Mono.just(new GetAccountBalanceResponse(
                        id, "5300000001", new BigDecimal("100.00"), "COP", "ACTIVE")));

        webTestClient.get()
                .uri("/api/v1/accounts/{id}/balance", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].accountId").isEqualTo(id.toString())
                .jsonPath("$.data[0].balance").isEqualTo(100.00);
    }

    @Test
    void streamBalance_returnsSseEvents() {
        UUID id = UUID.randomUUID();
        when(streamAccountBalanceInteractor.execute(id))
                .thenReturn(Flux.just(new GetAccountBalanceResponse(
                        id, "5300000001", new BigDecimal("100.00"), "COP", "ACTIVE")));

        webTestClient.get()
                .uri("/api/v1/accounts/{id}/balance/stream", id)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBody()
                .consumeWith(result -> {
                    String body = new String(result.getResponseBodyContent());
                    org.junit.jupiter.api.Assertions.assertTrue(body.contains("event:balance"));
                    org.junit.jupiter.api.Assertions.assertTrue(body.contains(id.toString()));
                    org.junit.jupiter.api.Assertions.assertTrue(body.contains("5300000001"));
                });
    }

    @Test
    void blockAccount_returns200() {
        UUID id = UUID.randomUUID();
        when(blockAccountInteractor.execute(id))
                .thenReturn(Mono.just(new AccountStatusResponse(
                        id, "5300000001", "BLOCKED", BigDecimal.ZERO)));

        webTestClient.mutateWith(csrf()).post()
                .uri("/api/v1/accounts/{id}/block", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].status").isEqualTo("BLOCKED")
                .jsonPath("$.messages[0]").isEqualTo(ResponseMessages.ACCOUNT_BLOCKED)
                .jsonPath("$.data[0].ownerId").doesNotExist();
    }

    @Test
    void unblockAccount_returns200() {
        UUID id = UUID.randomUUID();
        when(unblockAccountInteractor.execute(id))
                .thenReturn(Mono.just(new AccountStatusResponse(
                        id, "5300000001", "ACTIVE", BigDecimal.ZERO)));

        webTestClient.mutateWith(csrf()).post()
                .uri("/api/v1/accounts/{id}/unblock", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].status").isEqualTo("ACTIVE")
                .jsonPath("$.messages[0]").isEqualTo(ResponseMessages.ACCOUNT_UNBLOCKED);
    }

    @Test
    void closeAccount_returns200() {
        UUID id = UUID.randomUUID();
        when(closeAccountInteractor.execute(id))
                .thenReturn(Mono.just(new AccountStatusResponse(
                        id, "5300000001", "INACTIVE", BigDecimal.ZERO)));

        webTestClient.mutateWith(csrf()).post()
                .uri("/api/v1/accounts/{id}/close", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].status").isEqualTo("INACTIVE")
                .jsonPath("$.messages[0]").isEqualTo(ResponseMessages.ACCOUNT_CLOSED);
    }

    @Test
    void getBalance_returns404WhenAccountNotFound() {
        UUID id = UUID.randomUUID();
        when(getAccountBalanceInteractor.execute(id))
                .thenReturn(Mono.error(AccountNotFoundException.create(id)));

        webTestClient.get()
                .uri("/api/v1/accounts/{id}/balance", id)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("ACCOUNT_NOT_FOUND");
    }
}
