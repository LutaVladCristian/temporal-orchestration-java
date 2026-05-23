package com.example.springserver.controller;

import com.example.springserver.dto.ImportStatusResponseDto;
import com.example.springserver.dto.UploadCsvInputDto;
import com.example.springserver.dto.UploadCsvResponseDto;
import com.example.springserver.entity.IncomeFromSells;
import com.example.springserver.entity.OtherIncomeFees;
import com.example.springserver.repository.IncomeFromSellsRepository;
import com.example.springserver.repository.OtherIncomeFeesRepository;
import com.example.springserver.service.UploadCsvService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/spring-boot-api")
@RequiredArgsConstructor
public class UploadCsvController {

    private final UploadCsvService uploadCsvService;
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
        return ResponseEntity.ok(uploadCsvService.uploadCsv(input));
    }

    @GetMapping("/imports/{workflowId}")
    public ResponseEntity<ImportStatusResponseDto> importStatus(@PathVariable String workflowId) {
        ImportStatusResponseDto status = uploadCsvService.getImportStatus(workflowId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
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
