package com.fintrack.spending.controller;

import com.fintrack.spending.config.ApiKeyAuthFilter;
import com.fintrack.spending.config.SecurityConfig;
import com.fintrack.spending.domain.SpendingCategory;
import com.fintrack.spending.model.SpendingSummaryResponse;
import com.fintrack.spending.service.SpendingSummaryUpdater;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionAggregationController.class)
@Import({SecurityConfig.class, ApiKeyAuthFilter.class})
@TestPropertySource(properties = "api.key=test-api-key")
class TransactionAggregationControllerTest {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String TEST_API_KEY = "test-api-key";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpendingSummaryUpdater summaryUpdater;

    private SpendingSummaryResponse sampleSummary(String period) {
        return new SpendingSummaryResponse(
                UUID.randomUUID(),
                period,
                SpendingCategory.GROCERIES,
                "ZAR",
                new BigDecimal("1500.00")
        );
    }

    @Test
    void getSummary_returnsOk() throws Exception {
        when(summaryUpdater.getSummaryForPastMonth()).thenReturn(List.of(sampleSummary("2026-04")));

        mockMvc.perform(get("/transaction/summary").header(API_KEY_HEADER, TEST_API_KEY))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));

        verify(summaryUpdater).getSummaryForPastMonth();
    }

    @Test
    void getSummary_returnsEmptyList_whenNoData() throws Exception {
        when(summaryUpdater.getSummaryForPastMonth()).thenReturn(List.of());

        mockMvc.perform(get("/transaction/summary").header(API_KEY_HEADER, TEST_API_KEY))
                .andExpect(status().isOk());
    }

    @Test
    void getSummaryForPeriod_returnsOk_withMatchingPeriod() throws Exception {
        String period = "2026-03";
        when(summaryUpdater.getSummaryForPeriod(period)).thenReturn(List.of(sampleSummary(period)));

        mockMvc.perform(get("/transaction/summary/{period}", period).header(API_KEY_HEADER, TEST_API_KEY))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));

        verify(summaryUpdater).getSummaryForPeriod(period);
    }

    @Test
    void getSummaryForPeriod_returnsOk_whenNoDataForPeriod() throws Exception {
        String period = "2020-01";
        when(summaryUpdater.getSummaryForPeriod(period)).thenReturn(List.of());

        mockMvc.perform(get("/transaction/summary/{period}", period).header(API_KEY_HEADER, TEST_API_KEY))
                .andExpect(status().isOk());
    }

    @Test
    void getSummary_returns401_whenApiKeyMissing() throws Exception {
        mockMvc.perform(get("/transaction/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSummary_returns401_whenApiKeyInvalid() throws Exception {
        mockMvc.perform(get("/transaction/summary").header(API_KEY_HEADER, "wrong-key"))
                .andExpect(status().isUnauthorized());
    }
}
