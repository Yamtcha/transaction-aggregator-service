package com.fintrack.spending.config;

import com.fintrack.spending.domain.SpendingCategory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "spending.categorization")
public record CategorizationProperties(
        String noisePrefix,
        String referenceNoise,
        Map<SpendingCategory, List<String>> rules
) {
}
