package com.fintrack.spending.domain.entity;

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
        columnNames = {"source_id", "period", "currency", "merchant_name"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "source_id", nullable = false)
    private String sourceId;

    @Column(name = "period", nullable = false)
    private String period;

    @Column(name = "merchant_name", nullable = false)
    @Builder.Default
    private String merchantName = "UNKNOWN";

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "total_amount", nullable = false)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "transaction_count", nullable = false)
    @Builder.Default
    private int transactionCount = 0;

    @Column(name = "last_updated_at", nullable = false)
    @Builder.Default
    private Instant lastUpdatedAt = Instant.now();
}
