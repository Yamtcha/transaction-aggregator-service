package com.fintrack.spending.domain.repository;

import com.fintrack.spending.domain.entity.SpendingSummary;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpendingSummaryRepository extends JpaRepository<SpendingSummary, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SpendingSummary s WHERE s.sourceId = :sourceId AND s.period = :period " +
           "AND s.currency = :currency AND s.merchantName = :merchantName")
    Optional<SpendingSummary> findForUpdate(
            @Param("sourceId") String sourceId,
            @Param("period") String period,
            @Param("currency") String currency,
            @Param("merchantName") String merchantName);

    List<SpendingSummary> findBySourceIdOrderByPeriodDesc(String sourceId);

    List<SpendingSummary> findBySourceIdAndPeriodOrderByTotalAmountDesc(String sourceId, String period);
}
