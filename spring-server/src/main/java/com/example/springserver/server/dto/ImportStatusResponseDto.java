package com.example.springserver.server.dto;

import java.util.List;

public record ImportStatusResponseDto(
        String workflowId,
        String statementName,
        String status,
        String createdAt,
        String startedAt,
        String completedAt,
        List<ImportStepStatusDto> steps,
        List<String> failureMessages
) {
}
