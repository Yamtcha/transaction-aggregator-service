package com.fintrack.spending.dto.response;

import com.fintrack.spending.domain.SpendingCategory;
import com.fintrack.spending.domain.entity.SpendingSummary;

import java.math.BigDecimal;
import java.util.UUID;

public record SpendingSummaryResponse(
        UUID id,
        String period,
        SpendingCategory category,
        String currency,
        BigDecimal totalAmount
) {
    public static SpendingSummaryResponse from(SpendingSummary entity) {
        return new SpendingSummaryResponse(
                entity.getId(),
                entity.getPeriod(),
                entity.getCategory(),
                entity.getCurrency(),
                entity.getTotalAmount()
        );
    }
}
