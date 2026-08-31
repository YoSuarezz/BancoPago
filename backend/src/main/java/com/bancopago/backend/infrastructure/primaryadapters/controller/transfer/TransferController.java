package com.bancopago.backend.infrastructure.primaryadapters.controller.transfer;

import com.bancopago.backend.application.primaryports.dto.transfer.request.CreateTransferRequest;
import com.bancopago.backend.application.primaryports.dto.transfer.response.CreateTransferResponse;
import com.bancopago.backend.application.primaryports.dto.transfer.response.GetTransferResponse;
import com.bancopago.backend.application.primaryports.dto.transfer.response.ListTransferResponse;
import com.bancopago.backend.application.primaryports.interactor.transfer.CreateTransferInput;
import com.bancopago.backend.application.primaryports.interactor.transfer.CreateTransferInteractor;
import com.bancopago.backend.application.primaryports.interactor.transfer.GetTransferByIdInteractor;
import com.bancopago.backend.application.primaryports.interactor.transfer.ListTransfersByAccountInteractor;
import com.bancopago.backend.infrastructure.ResponseMessages;
import com.bancopago.backend.infrastructure.primaryadapters.adapter.response.ApiResponse;
import com.bancopago.backend.infrastructure.primaryadapters.adapter.response.HttpResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
@Tag(name = "Transfers")
public class TransferController {

    private final CreateTransferInteractor createTransferInteractor;
    private final GetTransferByIdInteractor getTransferByIdInteractor;
    private final ListTransfersByAccountInteractor listTransfersByAccountInteractor;

    public TransferController(CreateTransferInteractor createTransferInteractor,
                               GetTransferByIdInteractor getTransferByIdInteractor,
                               ListTransfersByAccountInteractor listTransfersByAccountInteractor) {
        this.createTransferInteractor = createTransferInteractor;
        this.getTransferByIdInteractor = getTransferByIdInteractor;
        this.listTransfersByAccountInteractor = listTransfersByAccountInteractor;
    }

    @PostMapping
    @Operation(summary = "Create a P2P transfer")
    public Mono<ResponseEntity<ApiResponse<CreateTransferResponse>>> createTransfer(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody @Valid CreateTransferRequest request) {
        var input = new CreateTransferInput(request, idempotencyKey);
        return HttpResponses.created(
                createTransferInteractor.execute(input),
                ResponseMessages.TRANSFER_CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transfer by ID")
    public Mono<ResponseEntity<ApiResponse<GetTransferResponse>>> getTransferById(
            @PathVariable UUID id) {
        return HttpResponses.ok(getTransferByIdInteractor.execute(id));
    }

    @GetMapping
    @Operation(summary = "List transfers by account number")
    public Mono<ResponseEntity<ApiResponse<List<ListTransferResponse>>>> listTransfersByAccount(
            @RequestParam String accountNumber) {
        return HttpResponses.ok(listTransfersByAccountInteractor.execute(accountNumber));
    }
}
