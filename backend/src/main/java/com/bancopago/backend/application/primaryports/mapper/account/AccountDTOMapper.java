package com.bancopago.backend.application.primaryports.mapper.account;

import com.bancopago.backend.application.primaryports.dto.account.response.ChangeAccountStatusResponse;
import com.bancopago.backend.application.primaryports.dto.account.response.CreateAccountResponse;
import com.bancopago.backend.application.primaryports.dto.account.response.GetAccountBalanceResponse;
import com.bancopago.backend.domain.account.AccountDomain;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class AccountDTOMapper {

    public abstract CreateAccountResponse toCreateAccountResponse(AccountDomain account);

    public abstract ChangeAccountStatusResponse toChangeAccountStatusResponse(AccountDomain account);

    @Mapping(source = "id", target = "accountId")
    @Mapping(source = "number", target = "accountNumber")
    public abstract GetAccountBalanceResponse toGetAccountBalanceResponse(AccountDomain account);
}
