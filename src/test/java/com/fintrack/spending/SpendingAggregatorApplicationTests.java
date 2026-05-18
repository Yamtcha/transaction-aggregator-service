package com.fintrack.spending;

import com.fintrack.spending.domain.repository.SpendingSummaryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SpendingAggregatorApplicationTests {

    @MockBean
    SpendingSummaryRepository spendingSummaryRepository;

    @MockBean
    ConnectionFactory connectionFactory;

    @Test
    void contextLoads() {
    }
}
