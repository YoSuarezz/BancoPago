package com.bancopago.backend.infrastructure.primaryadapters.controller.account;

import com.bancopago.backend.application.primaryports.dto.account.request.CreateAccountRequest;
import com.bancopago.backend.application.primaryports.dto.account.response.AccountStatusResponse;
import com.bancopago.backend.application.primaryports.dto.account.response.CreateAccountResponse;
import com.bancopago.backend.application.primaryports.dto.account.response.GetAccountBalanceResponse;
import com.bancopago.backend.application.primaryports.interactor.account.BlockAccountInteractor;
import com.bancopago.backend.application.primaryports.interactor.account.CloseAccountInteractor;
import com.bancopago.backend.application.primaryports.interactor.account.CreateAccountInteractor;
import com.bancopago.backend.application.primaryports.interactor.account.GetAccountBalanceInteractor;
import com.bancopago.backend.application.primaryports.interactor.account.UnblockAccountInteractor;
import com.bancopago.backend.infrastructure.GlobalExceptionHandler;
import com.bancopago.backend.infrastructure.ResponseMessages;
import com.bancopago.backend.infrastructure.secondaryadapters.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = AccountController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AccountControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CreateAccountInteractor createAccountInteractor;

    @MockitoBean
    private GetAccountBalanceInteractor getAccountBalanceInteractor;

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

        webTestClient.post()
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
    void blockAccount_returns200() {
        UUID id = UUID.randomUUID();
        when(blockAccountInteractor.execute(id))
                .thenReturn(Mono.just(new AccountStatusResponse(
                        id, "5300000001", "BLOCKED", BigDecimal.ZERO)));

        webTestClient.post()
                .uri("/api/v1/accounts/{id}/block", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].status").isEqualTo("BLOCKED")
                .jsonPath("$.messages[0]").isEqualTo(ResponseMessages.ACCOUNT_BLOCKED)
                .jsonPath("$.data[0].ownerId").doesNotExist();
    }
}
