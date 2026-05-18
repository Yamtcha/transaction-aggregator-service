package com.fintrack.spending.service;

import com.fintrack.common.model.Transaction;
import com.fintrack.spending.config.CategorizationProperties;
import com.fintrack.spending.domain.SpendingCategory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class CategoryResolver {

    private final CategorizationProperties properties;

    private Pattern noise;
    private Pattern refNoise;
    private List<CategoryRule> rules;

    private static CategoryRule buildRule(SpendingCategory category, List<String> patterns) {
        List<Pattern> compiled = patterns.stream()
                .map(p -> Pattern.compile(p, Pattern.CASE_INSENSITIVE))
                .toList();
        return new CategoryRule(category, compiled);
    }

    @PostConstruct
    void init() {
        noise = Pattern.compile(properties.noisePrefix());
        refNoise = Pattern.compile(properties.referenceNoise());
        rules = properties.rules().entrySet().stream()
                .map(entry -> buildRule(entry.getKey(), entry.getValue()))
                .toList();
    }

    public SpendingCategory resolve(Transaction tx) {
        String raw = Stream.of(tx.getMerchantName(), tx.getDescription())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));

        String normalized = normalize(raw);

        for (CategoryRule rule : rules) {
            if (rule.matches(normalized)) {
                return rule.category();
            }
        }
        return SpendingCategory.OTHER;
    }

    private String normalize(String raw) {
        String s = raw.toLowerCase();
        s = noise.matcher(s).replaceAll(" ");
        s = refNoise.matcher(s).replaceAll(" ");
        return s.trim();
    }

    private record CategoryRule(SpendingCategory category, List<Pattern> patterns) {
        boolean matches(String text) {
            return patterns.stream().anyMatch(p -> p.matcher(text).find());
        }
    }
}
