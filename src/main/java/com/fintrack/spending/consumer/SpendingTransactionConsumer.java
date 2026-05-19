package com.fintrack.spending.consumer;

import com.fintrack.common.events.TransactionIngestedEvent;
import com.fintrack.spending.service.SpendingSummaryUpdater;
import com.rabbitmq.client.Channel;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpendingTransactionConsumer {

    private final SpendingSummaryUpdater summaryUpdater;
    private final MeterRegistry meterRegistry;

    @RabbitListener(queues = "${rabbitmq.queues.transactions}", ackMode = "MANUAL", concurrency = "4-10")
    public void consume(TransactionIngestedEvent event,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            log.debug("Received spending event eventId={} externalId={}",
                    event.getEventId(), event.getTransaction().getExternalId());
            summaryUpdater.process(event);
            channel.basicAck(deliveryTag, false);
            sample.stop(meterRegistry.timer("transactions.consumed", "status", "success"));
        } catch (Exception ex) {
            log.error("Failed to process spending event eventId={}: {}",
                    event.getEventId(), ex.getMessage(), ex);
            channel.basicNack(deliveryTag, false, false);
            sample.stop(meterRegistry.timer("transactions.consumed", "status", "failure"));
        }
    }
}
