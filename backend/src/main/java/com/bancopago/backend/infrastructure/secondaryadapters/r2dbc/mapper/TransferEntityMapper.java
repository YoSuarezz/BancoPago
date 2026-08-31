package com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.mapper;

import com.bancopago.backend.crosscutting.helpers.TextHelper;
import com.bancopago.backend.domain.enums.Currency;
import com.bancopago.backend.domain.enums.TransferStatus;
import com.bancopago.backend.domain.transfer.TransferDomain;
import com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.entity.TransferEntity;
import org.springframework.stereotype.Component;

@Component
public class TransferEntityMapper {

    public TransferEntity toTransferEntity(TransferDomain domain) {
        if (domain == null) {
            return null;
        }
        TransferEntity entity = TransferEntity.create(
                domain.getId(),
                domain.getSourceAccountNumber(),
                domain.getTargetAccountNumber(),
                domain.getAmount(),
                domain.getCurrency() != null ? domain.getCurrency().name() : Currency.COP.name(),
                domain.getStatus() != null ? domain.getStatus().name() : TransferStatus.PENDING.name(),
                domain.getDescription(),
                domain.getIdempotencyKey(),
                0L,
                domain.getCreatedAt()
        );
        entity.markNew();
        return entity;
    }

    public TransferDomain toTransferDomain(TransferEntity entity) {
        if (entity == null) {
            return null;
        }
        return new TransferDomain(
                entity.getId(),
                entity.getSourceAccountNumber(),
                entity.getTargetAccountNumber(),
                entity.getAmount(),
                parseCurrency(entity.getCurrency()),
                parseTransferStatus(entity.getStatus()),
                entity.getDescription(),
                entity.getIdempotencyKey(),
                entity.getCreatedAt()
        );
    }

    private Currency parseCurrency(String value) {
        if (TextHelper.isBlank(value)) {
            return Currency.COP;
        }
        try {
            return Currency.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Currency.COP;
        }
    }

    private TransferStatus parseTransferStatus(String value) {
        if (TextHelper.isBlank(value)) {
            return TransferStatus.PENDING;
        }
        try {
            return TransferStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return TransferStatus.PENDING;
        }
    }
}
