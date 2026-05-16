package com.example.springserver.server.temporal.records;

public record ImportStepSnapshot(
        String stepName,
        String status,
        long readCount,
        long writeCount
) {
}
