package com.bancopago.backend.application.primaryports.dto.account.request;

import com.bancopago.backend.domain.enums.AccountOperation;
import jakarta.validation.constraints.NotNull;

public record ChangeAccountStatusRequest(
        @NotNull AccountOperation operation
) {
}
