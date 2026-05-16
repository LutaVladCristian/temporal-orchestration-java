package com.example.springserver.server.temporal.activities;

import com.example.springserver.server.entity.IncomeFromSells;
import com.example.springserver.server.entity.OtherIncomeFees;
import com.example.springserver.server.temporal.CsvSectionExtractor;
import com.example.springserver.server.repository.IncomeFromSellsRepository;
import com.example.springserver.server.repository.OtherIncomeFeesRepository;
import com.example.springserver.server.temporal.records.CsvImportRequest;
import com.example.springserver.server.temporal.records.ImportStepResult;
import com.example.springserver.server.temporal.records.StoredUploadRef;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CsvImportActivitiesImpl implements CsvImportActivities {

    private static final String SELLS_SECTION_HEADER = "Income from Sells";
    private static final String OTHER_INCOME_SECTION_HEADER = "Other income & fees";

    private final CsvSectionExtractor csvSectionExtractor;
    private final IncomeFromSellsRepository incomeFromSellsRepository;
    private final OtherIncomeFeesRepository otherIncomeFeesRepository;

    @Override
    @Transactional
    public ImportStepResult importSells(CsvImportRequest request) {
        String csv = readUpload(request.upload());
        String section = csvSectionExtractor.extractSection(csv, SELLS_SECTION_HEADER);
        List<String[]> rows = parseSection(section, SELLS_SECTION_HEADER);
        List<IncomeFromSells> entities = new ArrayList<>(rows.size());

        for (String[] columns : rows) {
            ensureColumnCount(columns, 11, SELLS_SECTION_HEADER);
            entities.add(IncomeFromSells.builder()
                    .dateAcquired(LocalDate.parse(columns[0]))
                    .dateSold(LocalDate.parse(columns[1]))
                    .symbol(columns[2])
                    .securityName(columns[3])
                    .isin(columns[4])
                    .country(columns[5])
                    .quantity(new BigDecimal(columns[6]))
                    .costBasis(new BigDecimal(columns[7]))
                    .grossProceeds(new BigDecimal(columns[8]))
                    .grossPnl(new BigDecimal(columns[9]))
                    .currency(columns[10])
                    .build());
        }

        incomeFromSellsRepository.saveAll(entities);
        return new ImportStepResult("sellsStep", rows.size(), entities.size());
    }

    @Override
    @Transactional
    public ImportStepResult importOtherIncome(CsvImportRequest request) {
        String csv = readUpload(request.upload());
        String section = csvSectionExtractor.extractSection(csv, OTHER_INCOME_SECTION_HEADER);
        List<String[]> rows = parseSection(section, OTHER_INCOME_SECTION_HEADER);
        List<OtherIncomeFees> entities = new ArrayList<>(rows.size());

        for (String[] columns : rows) {
            ensureColumnCount(columns, 9, OTHER_INCOME_SECTION_HEADER);
            entities.add(OtherIncomeFees.builder()
                    .date(LocalDate.parse(columns[0]))
                    .symbol(columns[1])
                    .securityName(columns[2])
                    .isin(columns[3])
                    .country(columns[4])
                    .grossAmount(new BigDecimal(columns[5]))
                    .withholdingTax(columns[6])
                    .netAmount(columns[7])
                    .currency(columns[8])
                    .build());
        }

        otherIncomeFeesRepository.saveAll(entities);
        return new ImportStepResult("otherIncomeStep", rows.size(), entities.size());
    }

    private String readUpload(StoredUploadRef upload) {
        try {
            return Files.readString(Path.of(upload.storagePath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read stored upload " + upload.originalFilename(), e);
        }
    }

    private List<String[]> parseSection(String section, String sectionName) {
        if (section == null || section.isBlank()) {
            throw new IllegalArgumentException("CSV section '" + sectionName + "' was not found.");
        }

        String[] lines = section.split("\\r?\\n");
        if (lines.length < 2) {
            throw new IllegalArgumentException("CSV section '" + sectionName + "' does not contain any data rows.");
        }

        List<String[]> rows = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                rows.add(splitCsvLine(line));
            }
        }
        return rows;
    }

    private String[] splitCsvLine(String line) {
        String[] rawColumns = line.split(",", -1);
        String[] trimmedColumns = new String[rawColumns.length];
        for (int i = 0; i < rawColumns.length; i++) {
            trimmedColumns[i] = rawColumns[i].trim();
        }
        return trimmedColumns;
    }

    private void ensureColumnCount(String[] columns, int expectedCount, String sectionName) {
        if (columns.length != expectedCount) {
            throw new IllegalArgumentException(
                    "CSV section '" + sectionName + "' expected " + expectedCount + " columns but found " + columns.length + ".");
        }
    }
}
