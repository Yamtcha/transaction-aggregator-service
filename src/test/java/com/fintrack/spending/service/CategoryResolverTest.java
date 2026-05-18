package com.fintrack.spending.service;

import com.fintrack.common.model.Transaction;
import com.fintrack.spending.config.CategorizationProperties;
import com.fintrack.spending.domain.SpendingCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CategoryResolverTest {

    private CategoryResolver resolver;

    @BeforeEach
    void setUp() {
        Map<SpendingCategory, List<String>> rules = new LinkedHashMap<>();
        rules.put(SpendingCategory.INCOME,        List.of("salary", "payroll", "refund", "cashback"));
        rules.put(SpendingCategory.UTILITIES,     List.of("eskom", "vodacom", "mtn", "airtime", "telkom"));
        rules.put(SpendingCategory.GROCERIES,     List.of("checkers", "shoprite", "woolworths", "spar", "pick\\s*n\\s*pay"));
        rules.put(SpendingCategory.DINING,        List.of("kfc", "mcdon(ald)?s?", "nandos?", "restaurant", "cafe"));
        rules.put(SpendingCategory.TRANSPORT,     List.of("uber", "bolt", "petrol", "fuel", "parking"));
        rules.put(SpendingCategory.SUBSCRIPTIONS, List.of("netflix", "spotify", "dstv", "showmax"));
        rules.put(SpendingCategory.HEALTHCARE,    List.of("dischem", "clicks", "pharmacy", "hospital"));
        rules.put(SpendingCategory.ENTERTAINMENT, List.of("cinema", "computicket", "virgin\\s*active"));
        rules.put(SpendingCategory.SHOPPING,      List.of("takealot", "mr\\s*price", "shein", "nike"));
        rules.put(SpendingCategory.TRANSFER,      List.of("eft", "bank\\s*transfer", "wallet"));

        CategorizationProperties properties = new CategorizationProperties(
                "(?i)^(card\\s*purchase|pos\\s*purchase|pos|acb\\s*(debit|credit)|debit\\s*order)[\\s:/-]*",
                "[*#|]|\\d{6,}",
                rules
        );

        resolver = new CategoryResolver(properties);
        resolver.init();
    }

    private Transaction txWithMerchant(String merchantName) {
        Transaction tx = mock(Transaction.class);
        when(tx.getMerchantName()).thenReturn(merchantName);
        when(tx.getDescription()).thenReturn(null);
        return tx;
    }

    private Transaction txWithDescription(String description) {
        Transaction tx = mock(Transaction.class);
        when(tx.getMerchantName()).thenReturn(null);
        when(tx.getDescription()).thenReturn(description);
        return tx;
    }

    private Transaction txWithBoth(String merchantName, String description) {
        Transaction tx = mock(Transaction.class);
        when(tx.getMerchantName()).thenReturn(merchantName);
        when(tx.getDescription()).thenReturn(description);
        return tx;
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "Checkers Fourways,         GROCERIES",
            "SHOPRITE KENILWORTH,       GROCERIES",
            "Pick n Pay Hypermarket,    GROCERIES",
            "KFC Drive Thru,            DINING",
            "McDonalds Rosebank,        DINING",
            "Nandos Sandton,            DINING",
            "Uber Trip,                 TRANSPORT",
            "Bolt Ride,                 TRANSPORT",
            "Shell Petrol Station,      TRANSPORT",
            "Netflix Monthly,           SUBSCRIPTIONS",
            "Spotify Premium,           SUBSCRIPTIONS",
            "DSTV Subscription,         SUBSCRIPTIONS",
            "Dis-Chem Pharmacy,         HEALTHCARE",
            "Clicks Health Store,       HEALTHCARE",
            "Takealot Order,            SHOPPING",
            "Mr Price Store,            SHOPPING",
            "Virgin Active Gym,         ENTERTAINMENT",
            "Computicket Event,         ENTERTAINMENT",
            "Eskom Prepaid,             UTILITIES",
            "Vodacom Airtime,           UTILITIES"
    })
    void resolve_matchesMerchantToCorrectCategory(String merchantName, String expected) {
        SpendingCategory result = resolver.resolve(txWithMerchant(merchantName.trim()));
        assertThat(result).isEqualTo(SpendingCategory.valueOf(expected.trim()));
    }

    @Test
    void resolve_matchesFromDescription_whenMerchantIsNull() {
        SpendingCategory result = resolver.resolve(txWithDescription("Netflix monthly subscription"));
        assertThat(result).isEqualTo(SpendingCategory.SUBSCRIPTIONS);
    }

    @Test
    void resolve_combinesMerchantAndDescription() {
        // merchant alone doesn't match, but description does
        SpendingCategory result = resolver.resolve(txWithBoth("Unknown Store", "salary payment from employer"));
        assertThat(result).isEqualTo(SpendingCategory.INCOME);
    }

    @Test
    void resolve_returnsOther_whenNoRuleMatches() {
        SpendingCategory result = resolver.resolve(txWithMerchant("Some Unrecognised Business XYZ"));
        assertThat(result).isEqualTo(SpendingCategory.OTHER);
    }

    @Test
    void resolve_returnsOther_whenMerchantAndDescriptionAreNull() {
        Transaction tx = mock(Transaction.class);
        when(tx.getMerchantName()).thenReturn(null);
        when(tx.getDescription()).thenReturn(null);

        SpendingCategory result = resolver.resolve(tx);
        assertThat(result).isEqualTo(SpendingCategory.OTHER);
    }

    @Test
    void resolve_isCaseInsensitive() {
        assertThat(resolver.resolve(txWithMerchant("CHECKERS"))).isEqualTo(SpendingCategory.GROCERIES);
        assertThat(resolver.resolve(txWithMerchant("checkers"))).isEqualTo(SpendingCategory.GROCERIES);
        assertThat(resolver.resolve(txWithMerchant("Checkers"))).isEqualTo(SpendingCategory.GROCERIES);
    }

    @Test
    void resolve_stripsNoisePrefix_beforeMatching() {
        // "POS PURCHASE KFC" -> after stripping noise prefix -> "kfc"
        SpendingCategory result = resolver.resolve(txWithMerchant("POS PURCHASE KFC Sandton"));
        assertThat(result).isEqualTo(SpendingCategory.DINING);
    }

    @Test
    void resolve_stripsReferenceNoise_beforeMatching() {
        // reference numbers (6+ digits) should be stripped
        SpendingCategory result = resolver.resolve(txWithMerchant("Checkers 123456789"));
        assertThat(result).isEqualTo(SpendingCategory.GROCERIES);
    }

    @Test
    void resolve_respectsRuleOrder_income_beforeTransfer() {
        // "refund" matches INCOME; ensure it's not misclassified
        SpendingCategory result = resolver.resolve(txWithMerchant("refund from merchant"));
        assertThat(result).isEqualTo(SpendingCategory.INCOME);
    }
}
