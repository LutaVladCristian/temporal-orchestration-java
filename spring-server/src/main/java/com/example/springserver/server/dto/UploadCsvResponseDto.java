package com.example.springserver.server.dto;

public record UploadCsvResponseDto(
        Long sellsJobExecutionId,
        Long otherIncomeJobExecutionId,
        String statementName
) {
}
