package com.fintrack.spending.service;

import com.fintrack.common.events.TransactionIngestedEvent;
import com.fintrack.common.model.Transaction;
import com.fintrack.spending.domain.SpendingCategory;
import com.fintrack.spending.domain.entity.SpendingSummary;
import com.fintrack.spending.domain.repository.SpendingSummaryRepository;
import com.fintrack.spending.model.SpendingSummaryResponse;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpendingSummaryUpdater {

    private final SpendingSummaryRepository summaryRepository;
    private final CategoryResolver categoryResolver;
    private final MeterRegistry meterRegistry;

    @Transactional
    public void process(TransactionIngestedEvent event) {

        Transaction transaction = event.getTransaction();

        String sourceId = transaction.getSourceId();
        String period = YearMonth.from(transaction.getTransactedAt().atZone(ZoneOffset.UTC)).toString();

        SpendingCategory category = categoryResolver.resolve(transaction);

        SpendingSummary summary = summaryRepository
                .findBySourceIdAndPeriodAndCategoryAndCurrency(
                        sourceId,
                        period,
                        category,
                        transaction.getCurrency()
                )
                .orElseGet(() -> SpendingSummary.builder()
                        .sourceId(sourceId)
                        .period(period)
                        .category(category)
                        .currency(transaction.getCurrency())
                        .totalAmount(BigDecimal.ZERO)
                        .transactionCount(0L)
                        .build());

        summary.setTotalAmount(summary.getTotalAmount().add(transaction.getAmount()));
        summary.setTransactionCount(summary.getTransactionCount() + 1);
        summary.setLastUpdatedAt(Instant.now());

        summaryRepository.save(summary);

        meterRegistry.counter("transactions.categorized", "category", category.name()).increment();
        log.debug("Updated summary sourceId={} period={} category={}", sourceId, period, category);
    }

    @Transactional(readOnly = true)
    public List<SpendingSummaryResponse> getSummaryForPastMonth() {
        String period = YearMonth.now(ZoneOffset.UTC).minusMonths(1).toString();
        return summaryRepository.findByPeriodOrderByTotalAmountDesc(period)
                .stream().map(SpendingSummaryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SpendingSummaryResponse> getSummaryForPeriod(String period) {
        return summaryRepository.findByPeriodOrderByTotalAmountDesc(period)
                .stream().map(SpendingSummaryResponse::from).toList();
    }
}
