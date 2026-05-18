package com.fintrack.spending.controller;

import com.fintrack.common.dto.ApiResponse;
import com.fintrack.spending.dto.response.SpendingSummaryResponse;
import com.fintrack.spending.service.SpendingSummaryUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spending")
@RequiredArgsConstructor
public class SpendingController {

    private final SpendingSummaryUpdater summaryUpdater;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<SpendingSummaryResponse>>> getSummary() {
        return ResponseEntity.ok(ApiResponse.of(summaryUpdater.getSummaryForPastMonth()));
    }

    @GetMapping("/summary/{period}")
    public ResponseEntity<ApiResponse<List<SpendingSummaryResponse>>> getSummaryForPeriod(
            @PathVariable String period) {
        return ResponseEntity.ok(ApiResponse.of(summaryUpdater.getSummaryForPeriod(period)));
    }
}
