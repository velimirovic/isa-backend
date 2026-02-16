package com.example.jutjubicconsumer.controller;

import com.example.jutjubicconsumer.messaging.UploadEventConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/api/benchmark")
public class BenchmarkController {

    private final UploadEventConsumer consumer;

    /**
     * Vraca metrike deserijalizacije prikupljene od strane consumera.
     * GET /api/benchmark/results
     */
    @GetMapping("/results")
    public ResponseEntity<Map<String, Object>> getResults() {
        List<Long> jsonTimes = consumer.getJsonDeserializationTimes();
        List<Long> protoTimes = consumer.getProtobufDeserializationTimes();

        Map<String, Object> results = new HashMap<>();
        results.put("jsonMessagesReceived", jsonTimes.size());
        results.put("protobufMessagesReceived", protoTimes.size());
        results.put("avgJsonDeserializationTimeNs", average(jsonTimes));
        results.put("avgProtobufDeserializationTimeNs", average(protoTimes));

        if (!jsonTimes.isEmpty() && !protoTimes.isEmpty()) {
            double avgJson = average(jsonTimes);
            double avgProto = average(protoTimes);
            results.put("deserializationSpeedupFactor", avgJson / avgProto);
        }

        return ResponseEntity.ok(results);
    }

    /**
     * Resetuje prikupljene metrike.
     * POST /api/benchmark/clear
     */
    @PostMapping("/clear")
    public ResponseEntity<String> clearResults() {
        consumer.clearTimings();
        return ResponseEntity.ok("Timings cleared");
    }

    private double average(List<Long> list) {
        if (list.isEmpty()) return 0;
        return list.stream().mapToLong(Long::longValue).average().orElse(0);
    }
}
