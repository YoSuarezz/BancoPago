package com.bancopago.backend.infrastructure.primaryadapters.controller.transfer;

import com.bancopago.backend.application.primaryports.dto.transfer.response.CreateTransferResponse;
import com.bancopago.backend.application.primaryports.dto.transfer.response.GetTransferResponse;
import com.bancopago.backend.application.primaryports.dto.transfer.response.ListTransferResponse;
import com.bancopago.backend.application.primaryports.interactor.transfer.CreateTransferInput;
import com.bancopago.backend.application.primaryports.interactor.transfer.CreateTransferInteractor;
import com.bancopago.backend.application.primaryports.interactor.transfer.GetTransferByIdInteractor;
import com.bancopago.backend.application.primaryports.interactor.transfer.ListTransfersByAccountInteractor;
import com.bancopago.backend.domain.transfer.exceptions.TransferNotFoundException;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = TransferController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser
class TransferControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CreateTransferInteractor createTransferInteractor;

    @MockitoBean
    private GetTransferByIdInteractor getTransferByIdInteractor;

    @MockitoBean
    private ListTransfersByAccountInteractor listTransfersByAccountInteractor;

    @Test
    void createTransfer_returns201() {
        UUID id = UUID.randomUUID();
        var now = LocalDateTime.now();
        when(createTransferInteractor.execute(any(CreateTransferInput.class)))
                .thenReturn(Mono.just(new CreateTransferResponse(
                        id, "1111111111", "2222222222",
                        new BigDecimal("100.00"), "COP", "COMPLETED",
                        "Test transfer", "idem-key-1", now)));

        webTestClient.mutateWith(csrf()).post()
                .uri("/api/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "idem-key-1")
                .bodyValue("""
                        {
                          "sourceAccountNumber": "1111111111",
                          "targetAccountNumber": "2222222222",
                          "amount": 100.00,
                          "description": "Test transfer"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data[0].id").isEqualTo(id.toString())
                .jsonPath("$.data[0].sourceAccountNumber").isEqualTo("1111111111")
                .jsonPath("$.data[0].targetAccountNumber").isEqualTo("2222222222")
                .jsonPath("$.data[0].status").isEqualTo("COMPLETED")
                .jsonPath("$.messages[0]").isEqualTo(ResponseMessages.TRANSFER_CREATED);
    }

    @Test
    void createTransfer_returns400WhenBodyInvalid() {
        webTestClient.mutateWith(csrf()).post()
                .uri("/api/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "idem-key-2")
                .bodyValue("""
                        {
                          "sourceAccountNumber": "",
                          "targetAccountNumber": "",
                          "amount": null
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void getTransferById_returns200() {
        UUID id = UUID.randomUUID();
        var now = LocalDateTime.now();
        when(getTransferByIdInteractor.execute(id))
                .thenReturn(Mono.just(new GetTransferResponse(
                        id, "1111111111", "2222222222",
                        new BigDecimal("50.00"), "COP", "COMPLETED",
                        null, now)));

        webTestClient.get()
                .uri("/api/v1/transfers/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].id").isEqualTo(id.toString())
                .jsonPath("$.data[0].amount").isEqualTo(50.00);
    }

    @Test
    void getTransferById_returns404WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(getTransferByIdInteractor.execute(id))
                .thenReturn(Mono.error(TransferNotFoundException.create(id)));

        webTestClient.get()
                .uri("/api/v1/transfers/{id}", id)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("TRANSFER_NOT_FOUND");
    }

    @Test
    void listTransfersByAccount_returns200() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        var now = LocalDateTime.now();
        when(listTransfersByAccountInteractor.execute("1111111111"))
                .thenReturn(Mono.just(List.of(
                        new ListTransferResponse(id1, "1111111111", "2222222222",
                                new BigDecimal("100.00"), "COP", "COMPLETED", null, now),
                        new ListTransferResponse(id2, "3333333333", "1111111111",
                                new BigDecimal("50.00"), "COP", "COMPLETED", "Pago", now)
                )));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/transfers")
                        .queryParam("accountNumber", "1111111111")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0][0].id").isEqualTo(id1.toString())
                .jsonPath("$.data[0][1].id").isEqualTo(id2.toString());
    }
}
