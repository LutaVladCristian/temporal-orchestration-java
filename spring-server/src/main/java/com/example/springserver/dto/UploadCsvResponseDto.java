package com.example.springserver.dto;

public record UploadCsvResponseDto(
        String importWorkflowId,
        String importRunId,
        String statementName
) {
}
