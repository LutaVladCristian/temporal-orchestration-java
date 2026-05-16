package com.example.springserver.server.dto;

public record UploadCsvResponseDto(
        String importWorkflowId,
        String importRunId,
        String statementName
) {
}
