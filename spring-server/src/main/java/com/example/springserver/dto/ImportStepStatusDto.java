package com.example.springserver.dto;

public record ImportStepStatusDto(
        String stepName,
        String status,
        long readCount,
        long writeCount
) {
}
