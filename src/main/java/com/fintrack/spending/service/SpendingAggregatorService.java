package com.fintrack.spending.service;

import com.fintrack.common.events.TransactionIngestedEvent;
import com.fintrack.common.model.Transaction;
import com.fintrack.spending.domain.entity.SpendingSummary;
import com.fintrack.spending.domain.entity.SpendingTransaction;
import com.fintrack.spending.domain.repository.SpendingSummaryRepository;
import com.fintrack.spending.domain.repository.SpendingTransactionRepository;
import com.fintrack.spending.dto.response.SpendingSummaryResponse;
import com.fintrack.spending.dto.response.SpendingTransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpendingAggregatorService {

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneOffset.UTC);

    private final SpendingTransactionRepository transactionRepository;
    private final SpendingSummaryRepository summaryRepository;

    @Transactional
    public void process(TransactionIngestedEvent event) {
        Transaction tx = event.getTransaction();

        if (transactionRepository.existsByExternalId(tx.getExternalId())) {
            log.info("Duplicate spending transaction externalId={}, skipping", tx.getExternalId());
            return;
        }

        SpendingTransaction spending = SpendingTransaction.builder()
                .eventId(event.getEventId())
                .externalId(tx.getExternalId())
                .sourceId(tx.getSourceId())
                .sourceType(tx.getSourceType() != null ? tx.getSourceType().name() : null)
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .merchantName(tx.getMerchantName())
                .description(tx.getDescription())
                .transactionType(tx.getType() != null ? tx.getType().name() : null)
                .transactedAt(tx.getTransactedAt())
                .build();

        transactionRepository.save(spending);
        updateSummary(tx);

        log.debug("Processed spending transaction externalId={}", tx.getExternalId());
    }

    @Transactional(readOnly = true)
    public List<SpendingTransactionResponse> getTransactions(String sourceId, Instant from, Instant to) {
        List<SpendingTransaction> transactions = (from != null && to != null)
                ? transactionRepository.findBySourceIdAndDateRange(sourceId, from, to)
                : transactionRepository.findBySourceIdOrderByTransactedAtDesc(sourceId);

        return transactions.stream().map(SpendingTransactionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SpendingSummaryResponse> getSummary(String sourceId) {
        return summaryRepository.findBySourceIdOrderByPeriodDesc(sourceId)
                .stream().map(SpendingSummaryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SpendingSummaryResponse> getSummaryForPeriod(String sourceId, String period) {
        return summaryRepository.findBySourceIdAndPeriodOrderByTotalAmountDesc(sourceId, period)
                .stream().map(SpendingSummaryResponse::from).toList();
    }

    private void updateSummary(Transaction tx) {
        String period = PERIOD_FORMATTER.format(tx.getTransactedAt());
        String merchantName = tx.getMerchantName() != null ? tx.getMerchantName() : "UNKNOWN";

        SpendingSummary summary = summaryRepository
                .findForUpdate(tx.getSourceId(), period, tx.getCurrency(), merchantName)
                .orElseGet(() -> SpendingSummary.builder()
                        .sourceId(tx.getSourceId())
                        .period(period)
                        .merchantName(merchantName)
                        .currency(tx.getCurrency())
                        .build());

        summary.setTotalAmount(summary.getTotalAmount().add(tx.getAmount()));
        summary.setTransactionCount(summary.getTransactionCount() + 1);
        summary.setLastUpdatedAt(Instant.now());

        summaryRepository.save(summary);
    }
}
