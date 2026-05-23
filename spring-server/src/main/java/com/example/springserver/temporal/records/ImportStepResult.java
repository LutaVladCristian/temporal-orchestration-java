package com.example.springserver.temporal.records;

public record ImportStepResult(
        String stepName,
        long readCount,
        long writeCount
) {
}
