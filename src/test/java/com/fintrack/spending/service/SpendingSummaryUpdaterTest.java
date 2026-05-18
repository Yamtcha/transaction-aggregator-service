package com.fintrack.spending.service;

import com.fintrack.common.events.TransactionIngestedEvent;
import com.fintrack.common.model.Transaction;
import com.fintrack.spending.domain.SpendingCategory;
import com.fintrack.spending.domain.entity.SpendingSummary;
import com.fintrack.spending.domain.repository.SpendingSummaryRepository;
import com.fintrack.spending.model.SpendingSummaryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpendingSummaryUpdaterTest {

    @Mock
    private SpendingSummaryRepository summaryRepository;

    @Mock
    private CategoryResolver categoryResolver;

    @InjectMocks
    private SpendingSummaryUpdater updater;

    private TransactionIngestedEvent transactionIngestedEvent(String sourceId, Instant transactedAt,
                                               String currency, BigDecimal amount,
                                               String merchantName,String description) {
        Transaction transaction = new  Transaction();

        transaction.setSourceId(sourceId);
        transaction.setTransactedAt(transactedAt);
        transaction.setCurrency(currency);
        transaction.setAmount(amount);
        transaction.setMerchantName(merchantName);
        transaction.setDescription(description);

        TransactionIngestedEvent event = new TransactionIngestedEvent();
        event.setTransaction(transaction);
        event.setEventId(UUID.randomUUID());
        event.setBatchId(UUID.randomUUID().toString());
        event.setOccurredAt(transactedAt);

        return event;
    }

    @Test
    void process_createsNewSummary_whenNoneExists() {
        Instant transactedAt = Instant.parse("2026-04-15T10:00:00Z");
        String period = YearMonth.from(transactedAt.atZone(ZoneOffset.UTC)).toString();
        TransactionIngestedEvent event = transactionIngestedEvent("src-1", transactedAt, "ZAR", new BigDecimal("250.00"), "Checkers","Milk");

        when(categoryResolver.resolve(any())).thenReturn(SpendingCategory.GROCERIES);
        when(summaryRepository.findBySourceIdAndPeriodAndCategoryAndCurrency(
                "src-1", period, SpendingCategory.GROCERIES, "ZAR"))
                .thenReturn(Optional.empty());

        updater.process(event);

        ArgumentCaptor<SpendingSummary> captor = ArgumentCaptor.forClass(SpendingSummary.class);
        verify(summaryRepository).save(captor.capture());

        SpendingSummary saved = captor.getValue();
        assertThat(saved.getSourceId()).isEqualTo("src-1");
        assertThat(saved.getPeriod()).isEqualTo(period);
        assertThat(saved.getCategory()).isEqualTo(SpendingCategory.GROCERIES);
        assertThat(saved.getCurrency()).isEqualTo("ZAR");
        assertThat(saved.getTotalAmount()).isEqualByComparingTo("250.00");
        assertThat(saved.getTransactionCount()).isEqualTo(1L);
    }

    @Test
    void process_updatesExistingSummary_whenOneExists() {
        Instant transactedAt = Instant.parse("2026-04-20T14:00:00Z");
        String period = YearMonth.from(transactedAt.atZone(ZoneOffset.UTC)).toString();
        TransactionIngestedEvent event = transactionIngestedEvent("src-2", transactedAt, "ZAR", new BigDecimal("100.00"), "Woolworths", "Chicken");

        SpendingSummary existing = SpendingSummary.builder()
                .id(UUID.randomUUID())
                .sourceId("src-2")
                .period(period)
                .category(SpendingCategory.GROCERIES)
                .currency("ZAR")
                .totalAmount(new BigDecimal("400.00"))
                .transactionCount(2L)
                .build();

        when(categoryResolver.resolve(any())).thenReturn(SpendingCategory.GROCERIES);
        when(summaryRepository.findBySourceIdAndPeriodAndCategoryAndCurrency(
                "src-2", period, SpendingCategory.GROCERIES, "ZAR"))
                .thenReturn(Optional.of(existing));

        updater.process(event);

        ArgumentCaptor<SpendingSummary> captor = ArgumentCaptor.forClass(SpendingSummary.class);
        verify(summaryRepository).save(captor.capture());

        SpendingSummary saved = captor.getValue();
        assertThat(saved.getTotalAmount()).isEqualByComparingTo("500.00");
        assertThat(saved.getTransactionCount()).isEqualTo(3L);
    }

    @Test
    void process_setsLastUpdatedAt() {
        Instant transactedAt = Instant.parse("2026-03-10T08:00:00Z");
        String period = YearMonth.from(transactedAt.atZone(ZoneOffset.UTC)).toString();
        TransactionIngestedEvent event = transactionIngestedEvent("src-3", transactedAt, "USD", new BigDecimal("50.00"), "KFC", "Streetwise 5");

        when(categoryResolver.resolve(any())).thenReturn(SpendingCategory.DINING);
        when(summaryRepository.findBySourceIdAndPeriodAndCategoryAndCurrency(any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        Instant before = Instant.now();
        updater.process(event);
        Instant after = Instant.now();

        ArgumentCaptor<SpendingSummary> captor = ArgumentCaptor.forClass(SpendingSummary.class);
        verify(summaryRepository).save(captor.capture());
        assertThat(captor.getValue().getLastUpdatedAt()).isBetween(before, after);
    }

    @Test
    void getSummaryForPastMonth_returnsMappedResponses() {
        String period = YearMonth.now(ZoneOffset.UTC).minusMonths(1).toString();
        SpendingSummary summary = SpendingSummary.builder()
                .id(UUID.randomUUID())
                .period(period)
                .category(SpendingCategory.TRANSPORT)
                .currency("ZAR")
                .totalAmount(new BigDecimal("800.00"))
                .transactionCount(5L)
                .build();

        when(summaryRepository.findByPeriodOrderByTotalAmountDesc(period)).thenReturn(List.of(summary));

        List<SpendingSummaryResponse> result = updater.getSummaryForPastMonth();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).period()).isEqualTo(period);
        assertThat(result.get(0).category()).isEqualTo(SpendingCategory.TRANSPORT);
        assertThat(result.get(0).totalAmount()).isEqualByComparingTo("800.00");
    }

    @Test
    void getSummaryForPastMonth_returnsEmptyList_whenNoneFound() {
        String period = YearMonth.now(ZoneOffset.UTC).minusMonths(1).toString();
        when(summaryRepository.findByPeriodOrderByTotalAmountDesc(period)).thenReturn(List.of());

        List<SpendingSummaryResponse> result = updater.getSummaryForPastMonth();

        assertThat(result).isEmpty();
    }

    @Test
    void getSummaryForPeriod_delegatesToRepository_withGivenPeriod() {
        String period = "2025-12";
        SpendingSummary summary = SpendingSummary.builder()
                .id(UUID.randomUUID())
                .period(period)
                .category(SpendingCategory.UTILITIES)
                .currency("ZAR")
                .totalAmount(new BigDecimal("2000.00"))
                .transactionCount(3L)
                .build();

        when(summaryRepository.findByPeriodOrderByTotalAmountDesc(period)).thenReturn(List.of(summary));

        List<SpendingSummaryResponse> result = updater.getSummaryForPeriod(period);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).period()).isEqualTo(period);
        verify(summaryRepository).findByPeriodOrderByTotalAmountDesc(period);
    }
}
