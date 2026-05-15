package com.example.springserver.server;

import com.example.springserver.server.dto.JobStatusResponseDto;
import com.example.springserver.server.dto.JobStepStatusDto;
import com.example.springserver.server.dto.UploadCsvInputDto;
import com.example.springserver.server.dto.UploadCsvResponseDto;
import com.example.springserver.server.entity.IncomeFromSells;
import com.example.springserver.server.entity.OtherIncomeFees;
import com.example.springserver.server.repository.IncomeFromSellsRepository;
import com.example.springserver.server.repository.OtherIncomeFeesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/spring-boot-api")
@RequiredArgsConstructor
public class UploadCsvController {

    private final UploadCsvService uploadCsvService;
    private final JobExplorer jobExplorer;
    private final IncomeFromSellsRepository incomeFromSellsRepository;
    private final OtherIncomeFeesRepository otherIncomeFeesRepository;

    @PostMapping("/upload-csv")
    public ResponseEntity<UploadCsvResponseDto> uploadCsv(
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) {

        UploadCsvInputDto input = UploadCsvInputDto.builder()
                .name(name)
                .file(file)
                .build();
        long jobExecutionId = uploadCsvService.uploadCsv(input);
        return ResponseEntity.ok(new UploadCsvResponseDto(jobExecutionId, name));
    }

    @GetMapping("/job-status/{executionId}")
    public ResponseEntity<JobStatusResponseDto> jobStatus(@PathVariable Long executionId) {
        var execution = jobExplorer.getJobExecution(executionId);
        if (execution == null) {
            return ResponseEntity.notFound().build();
        }
        BatchStatus status = execution.getStatus();
        List<JobStepStatusDto> steps = execution.getStepExecutions().stream()
                .sorted(Comparator.comparing(StepExecution::getStepName))
                .map(stepExecution -> new JobStepStatusDto(
                        stepExecution.getStepName(),
                        stepExecution.getStatus().name(),
                        stepExecution.getReadCount(),
                        stepExecution.getWriteCount(),
                        stepExecution.getCommitCount()))
                .toList();

        List<String> failureMessages = execution.getAllFailureExceptions().stream()
                .map(Throwable::getMessage)
                .filter(Objects::nonNull)
                .toList();

        return ResponseEntity.ok(new JobStatusResponseDto(
                execution.getId(),
                execution.getJobInstance().getJobName(),
                status.name(),
                execution.getExitStatus().getExitCode(),
                execution.getCreateTime(),
                execution.getStartTime(),
                execution.getEndTime(),
                execution.getLastUpdated(),
                steps,
                failureMessages));
    }

    @GetMapping("/income-from-sells")
    public ResponseEntity<List<IncomeFromSells>> getIncomeFromSells() {
        return ResponseEntity.ok(incomeFromSellsRepository.findAll(
                Sort.by(Sort.Order.desc("dateSold"), Sort.Order.desc("id"))));
    }

    @GetMapping("/other-income-fees")
    public ResponseEntity<List<OtherIncomeFees>> getOtherIncomeFees() {
        return ResponseEntity.ok(otherIncomeFeesRepository.findAll(
                Sort.by(Sort.Order.desc("date"), Sort.Order.desc("id"))));
    }
}
