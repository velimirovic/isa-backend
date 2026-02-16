package com.example.jutjubic.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "video-transcoding-queue";
    public static final String EXCHANGE_NAME = "video-transcoding-exchange";
    public static final String ROUTING_KEY = "video.transcode";

    @Bean
    public Queue transcodingQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange transcodingExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding transcodingBinding(Queue transcodingQueue, DirectExchange transcodingExchange) {
        return BindingBuilder.bind(transcodingQueue).to(transcodingExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setPrefetchCount(1);
        factory.setConcurrentConsumers(2);
        return factory;
    }
}
