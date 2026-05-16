package com.example.springserver.temporal.records;

public record ImportStepSnapshot(
        String stepName,
        String status,
        long readCount,
        long writeCount
) {
}
