package com.fintrack.spending.domain.repository;

import com.fintrack.spending.domain.entity.SpendingTransaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SpendingTransactionRepository extends JpaRepository<SpendingTransaction, UUID> {

    boolean existsByExternalId(String externalId);

    List<SpendingTransaction> findBySourceIdOrderByTransactedAtDesc(String sourceId);

    @Query("SELECT t FROM SpendingTransaction t WHERE t.sourceId = :sourceId " +
           "AND t.transactedAt >= :from AND t.transactedAt <= :to ORDER BY t.transactedAt DESC")
    List<SpendingTransaction> findBySourceIdAndDateRange(
            @Param("sourceId") String sourceId,
            @Param("from") Instant from,
            @Param("to") Instant to);
}
