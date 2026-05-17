package com.fintrack.spending.dto.response;

import com.fintrack.spending.domain.entity.SpendingTransaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SpendingTransactionResponse(
        UUID id,
        String externalId,
        String sourceId,
        BigDecimal amount,
        String currency,
        String merchantName,
        String description,
        String transactionType,
        Instant transactedAt,
        Instant receivedAt
) {
    public static SpendingTransactionResponse from(SpendingTransaction entity) {
        return new SpendingTransactionResponse(
                entity.getId(),
                entity.getExternalId(),
                entity.getSourceId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getMerchantName(),
                entity.getDescription(),
                entity.getTransactionType(),
                entity.getTransactedAt(),
                entity.getReceivedAt()
        );
    }
}
