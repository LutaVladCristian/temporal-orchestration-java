package com.example.springserver.temporal.records;

public record CsvImportRequest(
        String statementName,
        StoredUploadRef upload
) {
}
