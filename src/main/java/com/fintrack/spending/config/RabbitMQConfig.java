package com.fintrack.spending.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TRANSACTIONS_EXCHANGE = "fintrack.transactions";
    public static final String DLX_EXCHANGE = "fintrack.dlx";

    public static final String SPENDING_QUEUE = "fintrack.spending";
    public static final String DEAD_LETTER_QUEUE = "fintrack.dead-letter";

    public static final String ROUTING_KEY_SPENDING = "transaction.SPENDING";

    @Bean
    public TopicExchange transactionsExchange() {
        return ExchangeBuilder.topicExchange(TRANSACTIONS_EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(DLX_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue spendingQueue() {
        return QueueBuilder.durable(SPENDING_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SPENDING_QUEUE)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding spendingBinding(Queue spendingQueue, TopicExchange transactionsExchange) {
        return BindingBuilder.bind(spendingQueue).to(transactionsExchange).with(ROUTING_KEY_SPENDING);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(SPENDING_QUEUE);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
