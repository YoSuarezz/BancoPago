package com.bancopago.backend.application.primaryports.mapper.transfer;

import com.bancopago.backend.application.primaryports.dto.transfer.response.CreateTransferResponse;
import com.bancopago.backend.application.primaryports.dto.transfer.response.GetTransferResponse;
import com.bancopago.backend.application.primaryports.dto.transfer.response.ListTransferResponse;
import com.bancopago.backend.domain.transfer.TransferDomain;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransferDTOMapper {

    CreateTransferResponse toCreateTransferResponse(TransferDomain transfer);

    GetTransferResponse toGetTransferResponse(TransferDomain transfer);

    ListTransferResponse toListTransferResponse(TransferDomain transfer);
}
