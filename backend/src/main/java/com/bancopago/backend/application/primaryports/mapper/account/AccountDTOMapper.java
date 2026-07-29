package com.bancopago.backend.application.primaryports.mapper.account;

import com.bancopago.backend.application.primaryports.dto.account.response.AccountStatusResponse;
import com.bancopago.backend.application.primaryports.dto.account.response.CreateAccountResponse;
import com.bancopago.backend.application.primaryports.dto.account.response.GetAccountBalanceResponse;
import com.bancopago.backend.domain.account.AccountDomain;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountDTOMapper {

    CreateAccountResponse toCreateAccountResponse(AccountDomain account);

    AccountStatusResponse toAccountStatusResponse(AccountDomain account);

    @Mapping(source = "id", target = "accountId")
    @Mapping(source = "number", target = "accountNumber")
    GetAccountBalanceResponse toGetAccountBalanceResponse(AccountDomain account);
}
