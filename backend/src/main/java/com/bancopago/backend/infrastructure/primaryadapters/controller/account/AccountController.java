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
import com.bancopago.backend.infrastructure.ResponseMessages;
import com.bancopago.backend.infrastructure.primaryadapters.adapter.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts")
public class AccountController {

    private final CreateAccountInteractor createAccountInteractor;
    private final GetAccountBalanceInteractor getAccountBalanceInteractor;
    private final BlockAccountInteractor blockAccountInteractor;
    private final UnblockAccountInteractor unblockAccountInteractor;
    private final CloseAccountInteractor closeAccountInteractor;

    public AccountController(
            CreateAccountInteractor createAccountInteractor,
            GetAccountBalanceInteractor getAccountBalanceInteractor,
            BlockAccountInteractor blockAccountInteractor,
            UnblockAccountInteractor unblockAccountInteractor,
            CloseAccountInteractor closeAccountInteractor) {
        this.createAccountInteractor = createAccountInteractor;
        this.getAccountBalanceInteractor = getAccountBalanceInteractor;
        this.blockAccountInteractor = blockAccountInteractor;
        this.unblockAccountInteractor = unblockAccountInteractor;
        this.closeAccountInteractor = closeAccountInteractor;
    }

    @PostMapping
    @Operation(summary = "Create an account")
    public Mono<ResponseEntity<ApiResponse<CreateAccountResponse>>> createAccount(
            @RequestBody @Valid CreateAccountRequest request) {
        return createAccountInteractor.execute(request)
                .map(dto -> ApiResponse.of(dto, ResponseMessages.ACCOUNT_CREATED))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Get account balance")
    public Mono<ResponseEntity<ApiResponse<GetAccountBalanceResponse>>> getAccountBalance(
            @PathVariable UUID id) {
        return getAccountBalanceInteractor.execute(id)
                .map(ApiResponse::of)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/block")
    @Operation(summary = "Block an account")
    public Mono<ResponseEntity<ApiResponse<AccountStatusResponse>>> blockAccount(@PathVariable UUID id) {
        return blockAccountInteractor.execute(id)
                .map(dto -> ApiResponse.of(dto, ResponseMessages.ACCOUNT_BLOCKED))
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/unblock")
    @Operation(summary = "Unblock an account")
    public Mono<ResponseEntity<ApiResponse<AccountStatusResponse>>> unblockAccount(@PathVariable UUID id) {
        return unblockAccountInteractor.execute(id)
                .map(dto -> ApiResponse.of(dto, ResponseMessages.ACCOUNT_UNBLOCKED))
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close an account")
    public Mono<ResponseEntity<ApiResponse<AccountStatusResponse>>> closeAccount(@PathVariable UUID id) {
        return closeAccountInteractor.execute(id)
                .map(dto -> ApiResponse.of(dto, ResponseMessages.ACCOUNT_CLOSED))
                .map(ResponseEntity::ok);
    }
}
