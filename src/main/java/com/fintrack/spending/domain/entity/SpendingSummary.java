package com.fintrack.spending.domain.entity;

import com.fintrack.spending.domain.SpendingCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "spending_summary",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_spending_summary",
            columnNames = {"period", "currency", "category"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingSummary {
    @Id
    @GeneratedValue
    private UUID id;

    private String sourceId;
    private String period;

    @Enumerated(EnumType.STRING)
    private SpendingCategory category;

    private String currency;

    private BigDecimal totalAmount;

    private Long transactionCount;

    private Instant lastUpdatedAt;
}