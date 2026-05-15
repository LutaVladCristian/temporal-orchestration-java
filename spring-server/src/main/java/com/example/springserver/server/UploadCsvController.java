package com.example.springserver.server;

import com.example.springserver.server.dto.UploadCsvInputDto;
import com.example.springserver.server.entity.IncomeFromSells;
import com.example.springserver.server.entity.OtherIncomeFees;
import com.example.springserver.server.repository.IncomeFromSellsRepository;
import com.example.springserver.server.repository.OtherIncomeFeesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/spring-boot-api")
@RequiredArgsConstructor
public class UploadCsvController {

    private final UploadCsvService uploadCsvService;
    private final JobExplorer jobExplorer;
    private final IncomeFromSellsRepository incomeFromSellsRepository;
    private final OtherIncomeFeesRepository otherIncomeFeesRepository;

    @PostMapping("/upload-csv")
    public ResponseEntity<Map<String, Long>> uploadCsv(
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) {

        UploadCsvInputDto input = UploadCsvInputDto.builder()
                .name(name)
                .file(file)
                .build();
        long jobExecutionId = uploadCsvService.uploadCsv(input);
        return ResponseEntity.ok(Map.of("jobExecutionId", jobExecutionId));
    }

    @GetMapping("/job-status/{executionId}")
    public ResponseEntity<Map<String, String>> jobStatus(@PathVariable Long executionId) {
        var execution = jobExplorer.getJobExecution(executionId);
        if (execution == null) {
            return ResponseEntity.notFound().build();
        }
        BatchStatus status = execution.getStatus();
        return ResponseEntity.ok(Map.of("status", status.name()));
    }

    @GetMapping("/income-from-sells")
    public ResponseEntity<List<IncomeFromSells>> getIncomeFromSells() {
        return ResponseEntity.ok(incomeFromSellsRepository.findAll());
    }

    @GetMapping("/other-income-fees")
    public ResponseEntity<List<OtherIncomeFees>> getOtherIncomeFees() {
        return ResponseEntity.ok(otherIncomeFeesRepository.findAll());
    }
}
