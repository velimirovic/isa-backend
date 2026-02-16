package com.example.jutjubic.api.dto.videopost;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BenchmarkResultDTO {
    private int messageCount;

    private double avgJsonSerializationTimeNs;
    private double avgProtobufSerializationTimeNs;

    private double avgJsonDeserializationTimeNs;
    private double avgProtobufDeserializationTimeNs;

    private long jsonMessageSizeBytes;
    private long protobufMessageSizeBytes;

    private double serializationSpeedupFactor;
    private double deserializationSpeedupFactor;
    private double sizeReductionFactor;
}
