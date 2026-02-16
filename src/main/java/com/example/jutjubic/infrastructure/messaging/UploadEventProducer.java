package com.example.jutjubic.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;

import com.example.jutjubic.api.dto.videopost.UploadEventDTO;
import com.example.jutjubic.infrastructure.messaging.proto.UploadEventProto;


@Slf4j
@Service
@RequiredArgsConstructor
public class UploadEventProducer {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${myexchange}")
    private String exchange;

    @Value("${routingkey.json}")
    private String jsonRoutingKey;

    @Value("${routingkey.protobuf}")
    private String protobufRoutingKey;

    /**
     * Serijalizuje UploadEventDTO u JSON byte[] i salje na RabbitMQ.
     * Vraca niz: [serializationTimeNanos, messageSizeBytes]
     */
    public long[] sendJsonMessage(UploadEventDTO event) throws Exception {
        long start = System.nanoTime();
        byte[] jsonBytes = objectMapper.writeValueAsBytes(event);
        long serializationTime = System.nanoTime() - start;

        rabbitTemplate.convertAndSend(exchange, jsonRoutingKey, jsonBytes);
        log.info("Sent JSON message, size={} bytes, serTime={}ns", jsonBytes.length, serializationTime);

        return new long[]{serializationTime, jsonBytes.length};
    }

    /**
     * Serijalizuje UploadEventDTO u Protobuf byte[] i salje na RabbitMQ.
     * Vraca niz: [serializationTimeNanos, messageSizeBytes]
     */
    public long[] sendProtobufMessage(UploadEventDTO event) {
        long start = System.nanoTime();
        UploadEventProto.UploadEvent protoEvent = UploadEventProto.UploadEvent.newBuilder()
                .setDraftId(event.getDraftId())
                .setTitle(event.getTitle())
                .setDescription(event.getDescription())
                .setAuthorUsername(event.getAuthorUsername())
                .setAuthorEmail(event.getAuthorEmail())
                .setFileSizeBytes(event.getFileSizeBytes())
                .setDurationSeconds(event.getDurationSeconds())
                .setResolution(event.getResolution())
                .setUploadTimestamp(event.getUploadTimestamp())
                .build();
        byte[] protoBytes = protoEvent.toByteArray();
        long serializationTime = System.nanoTime() - start;

        rabbitTemplate.convertAndSend(exchange, protobufRoutingKey, protoBytes);
        log.info("Sent Protobuf message, size={} bytes, serTime={}ns", protoBytes.length, serializationTime);

        return new long[]{serializationTime, protoBytes.length};
    }
}
