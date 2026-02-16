package com.example.jutjubic.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {

    @Value("${myqueue.json}")
    private String jsonQueueName;

    @Value("${myqueue.protobuf}")
    private String protobufQueueName;

    @Value("${myexchange}")
    private String exchangeName;

    @Value("${routingkey.json}")
    private String jsonRoutingKey;

    @Value("${routingkey.protobuf}")
    private String protobufRoutingKey;

    @Bean
    Queue jsonQueue() {
        return new Queue(jsonQueueName, false);
    }

    @Bean
    Queue protobufQueue() {
        return new Queue(protobufQueueName, false);
    }

    @Bean
    DirectExchange exchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    Binding jsonBinding(Queue jsonQueue, DirectExchange exchange) {
        return BindingBuilder.bind(jsonQueue).to(exchange).with(jsonRoutingKey);
    }

    @Bean
    Binding protobufBinding(Queue protobufQueue, DirectExchange exchange) {
        return BindingBuilder.bind(protobufQueue).to(exchange).with(protobufRoutingKey);
    }
}
