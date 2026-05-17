package com.fintrack.spending.controller;

import com.fintrack.common.dto.ApiResponse;
import com.fintrack.spending.dto.response.SpendingSummaryResponse;
import com.fintrack.spending.dto.response.SpendingTransactionResponse;
import com.fintrack.spending.service.SpendingAggregatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/spending")
@RequiredArgsConstructor
public class SpendingController {

    private final SpendingAggregatorService aggregatorService;

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<SpendingTransactionResponse>>> getTransactions(
            @RequestParam String sourceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return ResponseEntity.ok(ApiResponse.of(aggregatorService.getTransactions(sourceId, from, to)));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<SpendingSummaryResponse>>> getSummary(
            @RequestParam String sourceId) {
        return ResponseEntity.ok(ApiResponse.of(aggregatorService.getSummary(sourceId)));
    }

    @GetMapping("/summary/{period}")
    public ResponseEntity<ApiResponse<List<SpendingSummaryResponse>>> getSummaryForPeriod(
            @PathVariable String period,
            @RequestParam String sourceId) {
        return ResponseEntity.ok(ApiResponse.of(aggregatorService.getSummaryForPeriod(sourceId, period)));
    }
}
