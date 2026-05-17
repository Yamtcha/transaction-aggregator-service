package com.fintrack.spending.service;

import com.fintrack.common.domain.TransactionClass;
import com.fintrack.common.domain.TransactionType;
import com.fintrack.common.events.TransactionIngestedEvent;
import com.fintrack.common.model.Transaction;
import com.fintrack.spending.domain.entity.SpendingTransaction;
import com.fintrack.spending.domain.repository.SpendingSummaryRepository;
import com.fintrack.spending.domain.repository.SpendingTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpendingAggregatorServiceTest {

    @Mock
    private SpendingTransactionRepository transactionRepository;

    @Mock
    private SpendingSummaryRepository summaryRepository;

    @InjectMocks
    private SpendingAggregatorService aggregatorService;

    @Test
    void process_savesTransactionAndUpdatesSummary() {
        TransactionIngestedEvent event = buildEvent("ext-001");
        when(transactionRepository.existsByExternalId("ext-001")).thenReturn(false);
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(summaryRepository.findForUpdate(any(), any(), any(), any())).thenReturn(Optional.empty());
        when(summaryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        aggregatorService.process(event);

        verify(transactionRepository).save(any(SpendingTransaction.class));
        verify(summaryRepository).save(any());
    }

    @Test
    void process_skipsDuplicateTransaction() {
        TransactionIngestedEvent event = buildEvent("ext-duplicate");
        when(transactionRepository.existsByExternalId("ext-duplicate")).thenReturn(true);

        aggregatorService.process(event);

        verify(transactionRepository, never()).save(any());
        verify(summaryRepository, never()).save(any());
    }

    private TransactionIngestedEvent buildEvent(String externalId) {
        Transaction tx = Transaction.builder()
                .id(UUID.randomUUID())
                .externalId(externalId)
                .sourceId("source-123")
                .amount(new BigDecimal("150.00"))
                .currency("ZAR")
                .merchantName("Checkers")
                .transactionClass(TransactionClass.SPENDING)
                .type(TransactionType.DEBIT)
                .transactedAt(Instant.now())
                .build();

        return TransactionIngestedEvent.builder()
                .eventId(UUID.randomUUID())
                .occurredAt(Instant.now())
                .transaction(tx)
                .build();
    }
}
