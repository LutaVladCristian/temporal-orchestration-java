package com.example.springserver.server.dto;

public record UploadCsvResponseDto(
        Long jobExecutionId,
        String statementName
) {
}
