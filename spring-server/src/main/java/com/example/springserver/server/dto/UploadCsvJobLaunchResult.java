package com.example.springserver.server.dto;

public record UploadCsvJobLaunchResult(
        Long sellsJobExecutionId,
        Long otherIncomeJobExecutionId
) {
}
