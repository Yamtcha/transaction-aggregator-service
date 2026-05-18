package com.fintrack.spending.domain.repository;

import com.fintrack.spending.domain.SpendingCategory;
import com.fintrack.spending.domain.entity.SpendingSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpendingSummaryRepository extends JpaRepository<SpendingSummary, Long> {
    Optional<SpendingSummary> findBySourceIdAndPeriodAndCategoryAndCurrency(
            String sourceId,
            String period,
            SpendingCategory category,
            String currency
    );

    List<SpendingSummary> findByPeriodOrderByTotalAmountDesc(String period);
}
