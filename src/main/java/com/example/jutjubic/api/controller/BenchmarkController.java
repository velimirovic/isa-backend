package com.example.jutjubic.api.controller;

import com.example.jutjubic.api.dto.videopost.BenchmarkResultDTO;
import com.example.jutjubic.api.dto.videopost.UploadEventDTO;
import com.example.jutjubic.infrastructure.messaging.UploadEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/api/benchmark")
public class BenchmarkController {

    private final UploadEventProducer producer;

    /**
     * Salje count poruka u oba formata (JSON i Protobuf) i vraca metrike serijalizacije.
     * GET /api/benchmark/send?count=50
     */
    @GetMapping("/send")
    public ResponseEntity<BenchmarkResultDTO> sendBenchmark(
            @RequestParam(defaultValue = "50") int count) throws Exception {

        long[] jsonSerTimes = new long[count];
        long[] protoSerTimes = new long[count];
        long jsonSize = 0;
        long protoSize = 0;

        for (int i = 0; i < count; i++) {
            UploadEventDTO event = UploadEventDTO.builder()
                    .draftId("bench-" + i)
                    .title("Benchmark Video #" + i)
                    .description("Ovo je benchmark test video broj " + i + " za poredjenje JSON vs Protobuf serijalizacije")
                    .authorUsername("benchmarkUser")
                    .authorEmail("benchmark@jutjubic.com")
                    .fileSizeBytes(104857600L) // 100MB
                    .durationSeconds(300)
                    .resolution("1920x1080")
                    .uploadTimestamp(LocalDateTime.now().toString())
                    .build();

            long[] jsonResult = producer.sendJsonMessage(event);
            jsonSerTimes[i] = jsonResult[0];
            jsonSize = jsonResult[1];

            long[] protoResult = producer.sendProtobufMessage(event);
            protoSerTimes[i] = protoResult[0];
            protoSize = protoResult[1];
        }

        double avgJsonSer = average(jsonSerTimes);
        double avgProtoSer = average(protoSerTimes);

        BenchmarkResultDTO result = BenchmarkResultDTO.builder()
                .messageCount(count)
                .avgJsonSerializationTimeNs(avgJsonSer)
                .avgProtobufSerializationTimeNs(avgProtoSer)
                .jsonMessageSizeBytes(jsonSize)
                .protobufMessageSizeBytes(protoSize)
                .serializationSpeedupFactor(avgJsonSer / avgProtoSer)
                .sizeReductionFactor((double) jsonSize / protoSize)
                .build();

        return ResponseEntity.ok(result);
    }

    private double average(long[] arr) {
        long sum = 0;
        for (long v : arr) sum += v;
        return (double) sum / arr.length;
    }
}
