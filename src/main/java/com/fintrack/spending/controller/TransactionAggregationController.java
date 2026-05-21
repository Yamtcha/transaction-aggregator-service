package com.fintrack.spending.controller;

import com.fintrack.common.dto.ApiResponse;
import com.fintrack.spending.model.SpendingSummaryResponse;
import com.fintrack.spending.service.SpendingSummaryUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionAggregationController {

    private final SpendingSummaryUpdater summaryUpdater;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<SpendingSummaryResponse>>> getSummary() {
        log.error("Fetching spending summary for past month");
        List<SpendingSummaryResponse> result = summaryUpdater.getSummaryForPastMonth();
        log.error("Returning {} summary records for past month", result.size());
        return ResponseEntity.ok(ApiResponse.of(result));
    }

    @GetMapping("/summary/{period}")
    public ResponseEntity<ApiResponse<List<SpendingSummaryResponse>>> getSummaryForPeriod(
            @PathVariable String period) {
        log.error("Fetching spending summary for period={}", period);
        List<SpendingSummaryResponse> result = summaryUpdater.getSummaryForPeriod(period);
        log.error("Returning {} summary records for period={}", result.size(), period);
        return ResponseEntity.ok(ApiResponse.of(result));
    }
}
