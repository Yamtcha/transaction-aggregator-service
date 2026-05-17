package com.fintrack.spending.consumer;

import com.fintrack.common.events.TransactionIngestedEvent;
import com.fintrack.spending.config.RabbitMQConfig;
import com.fintrack.spending.service.SpendingAggregatorService;
import com.rabbitmq.client.Channel;
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

    private final SpendingAggregatorService aggregatorService;

    @RabbitListener(queues = RabbitMQConfig.SPENDING_QUEUE, ackMode = "MANUAL")
    public void consume(TransactionIngestedEvent event,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.debug("Received spending event eventId={} externalId={}",
                    event.getEventId(), event.getTransaction().getExternalId());
            aggregatorService.process(event);
            channel.basicAck(deliveryTag, false);
        } catch (Exception ex) {
            log.error("Failed to process spending event eventId={}: {}",
                    event.getEventId(), ex.getMessage(), ex);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
