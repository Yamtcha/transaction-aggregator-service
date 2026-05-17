package com.fintrack.spending.dto.response;

import com.fintrack.spending.domain.entity.SpendingSummary;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SpendingSummaryResponse(
        UUID id,
        String sourceId,
        String period,
        String merchantName,
        String currency,
        BigDecimal totalAmount,
        int transactionCount,
        Instant lastUpdatedAt
) {
    public static SpendingSummaryResponse from(SpendingSummary entity) {
        return new SpendingSummaryResponse(
                entity.getId(),
                entity.getSourceId(),
                entity.getPeriod(),
                entity.getMerchantName(),
                entity.getCurrency(),
                entity.getTotalAmount(),
                entity.getTransactionCount(),
                entity.getLastUpdatedAt()
        );
    }
}
