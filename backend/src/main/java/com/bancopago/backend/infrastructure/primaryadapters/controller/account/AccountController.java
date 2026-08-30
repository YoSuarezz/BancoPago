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
import com.bancopago.backend.infrastructure.ResponseMessages;
import com.bancopago.backend.infrastructure.primaryadapters.adapter.response.ApiResponse;
import com.bancopago.backend.infrastructure.primaryadapters.adapter.response.HttpResponses;
import com.bancopago.backend.infrastructure.primaryadapters.adapter.response.SseEvents;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts")
public class AccountController {

    private final CreateAccountInteractor createAccountInteractor;
    private final GetAccountBalanceInteractor getAccountBalanceInteractor;
    private final StreamAccountBalanceInteractor streamAccountBalanceInteractor;
    private final ListAccountsByOwnerInteractor listAccountsByOwnerInteractor;
    private final BlockAccountInteractor blockAccountInteractor;
    private final UnblockAccountInteractor unblockAccountInteractor;
    private final CloseAccountInteractor closeAccountInteractor;

    public AccountController(
            CreateAccountInteractor createAccountInteractor,
            GetAccountBalanceInteractor getAccountBalanceInteractor,
            StreamAccountBalanceInteractor streamAccountBalanceInteractor,
            ListAccountsByOwnerInteractor listAccountsByOwnerInteractor,
            BlockAccountInteractor blockAccountInteractor,
            UnblockAccountInteractor unblockAccountInteractor,
            CloseAccountInteractor closeAccountInteractor) {
        this.createAccountInteractor = createAccountInteractor;
        this.getAccountBalanceInteractor = getAccountBalanceInteractor;
        this.streamAccountBalanceInteractor = streamAccountBalanceInteractor;
        this.listAccountsByOwnerInteractor = listAccountsByOwnerInteractor;
        this.blockAccountInteractor = blockAccountInteractor;
        this.unblockAccountInteractor = unblockAccountInteractor;
        this.closeAccountInteractor = closeAccountInteractor;
    }

    @PostMapping
    @Operation(summary = "Create an account")
    public Mono<ResponseEntity<ApiResponse<CreateAccountResponse>>> createAccount(
            @RequestBody @Valid CreateAccountRequest request) {
        return HttpResponses.created(
                createAccountInteractor.execute(request),
                ResponseMessages.ACCOUNT_CREATED);
    }

    @GetMapping
    @Operation(summary = "List accounts by owner")
    public Mono<ResponseEntity<ApiResponse<ListAccountResponse>>> listAccountsByOwner(
            @RequestParam UUID ownerId) {
        return HttpResponses.okList(listAccountsByOwnerInteractor.execute(ownerId));
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Get account balance")
    public Mono<ResponseEntity<ApiResponse<GetAccountBalanceResponse>>> getAccountBalance(
            @PathVariable UUID id) {
        return HttpResponses.ok(getAccountBalanceInteractor.execute(id));
    }

    @GetMapping(value = "/{id}/balance/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream account balance (SSE)")
    public Flux<ServerSentEvent<GetAccountBalanceResponse>> streamAccountBalance(@PathVariable UUID id) {
        return SseEvents.map(streamAccountBalanceInteractor.execute(id), SseEvents.BALANCE);
    }

    @PostMapping("/{id}/block")
    @Operation(summary = "Block an account")
    public Mono<ResponseEntity<ApiResponse<AccountStatusResponse>>> blockAccount(@PathVariable UUID id) {
        return HttpResponses.ok(
                blockAccountInteractor.execute(id),
                ResponseMessages.ACCOUNT_BLOCKED);
    }

    @PostMapping("/{id}/unblock")
    @Operation(summary = "Unblock an account")
    public Mono<ResponseEntity<ApiResponse<AccountStatusResponse>>> unblockAccount(@PathVariable UUID id) {
        return HttpResponses.ok(
                unblockAccountInteractor.execute(id),
                ResponseMessages.ACCOUNT_UNBLOCKED);
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close an account")
    public Mono<ResponseEntity<ApiResponse<AccountStatusResponse>>> closeAccount(@PathVariable UUID id) {
        return HttpResponses.ok(
                closeAccountInteractor.execute(id),
                ResponseMessages.ACCOUNT_CLOSED);
    }
}
