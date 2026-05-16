package com.example.springserver.server.temporal.records;

public record ImportStepResult(
        String stepName,
        long readCount,
        long writeCount
) {
}
