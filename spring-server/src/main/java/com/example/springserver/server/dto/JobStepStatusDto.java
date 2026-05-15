package com.example.springserver.server.dto;

public record JobStepStatusDto(
        String stepName,
        String status,
        long readCount,
        long writeCount,
        long commitCount
) {
}
