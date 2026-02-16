package com.example.jutjubicconsumer.messaging;

import com.example.jutjubicconsumer.dto.UploadEventDTO;
import com.example.jutjubicconsumer.messaging.proto.UploadEventProto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class UploadEventConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Getter
    private final CopyOnWriteArrayList<Long> jsonDeserializationTimes = new CopyOnWriteArrayList<>();

    @Getter
    private final CopyOnWriteArrayList<Long> protobufDeserializationTimes = new CopyOnWriteArrayList<>();

    @RabbitListener(queues = "${myqueue.json}")
    public void receiveJsonMessage(byte[] message) {
        try {
            long start = System.nanoTime();
            UploadEventDTO event = objectMapper.readValue(message, UploadEventDTO.class);
            long deserializationTime = System.nanoTime() - start;

            jsonDeserializationTimes.add(deserializationTime);
            log.info("Received JSON: title={}, deserTime={}ns", event.getTitle(), deserializationTime);
        } catch (Exception e) {
            log.error("Failed to deserialize JSON message", e);
        }
    }

    @RabbitListener(queues = "${myqueue.protobuf}")
    public void receiveProtobufMessage(byte[] message) {
        try {
            long start = System.nanoTime();
            UploadEventProto.UploadEvent event = UploadEventProto.UploadEvent.parseFrom(message);
            long deserializationTime = System.nanoTime() - start;

            protobufDeserializationTimes.add(deserializationTime);
            log.info("Received Protobuf: title={}, deserTime={}ns", event.getTitle(), deserializationTime);
        } catch (Exception e) {
            log.error("Failed to deserialize Protobuf message", e);
        }
    }

    public void clearTimings() {
        jsonDeserializationTimes.clear();
        protobufDeserializationTimes.clear();
    }
}
