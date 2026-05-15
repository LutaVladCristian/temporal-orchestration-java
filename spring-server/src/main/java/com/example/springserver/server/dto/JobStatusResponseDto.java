package com.example.springserver.server.dto;

import java.time.LocalDateTime;
import java.util.List;

public record JobStatusResponseDto(
        Long executionId,
        String jobName,
        String status,
        String exitCode,
        LocalDateTime createTime,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime lastUpdated,
        List<JobStepStatusDto> steps,
        List<String> failureMessages
) {
}
