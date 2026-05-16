package com.example.springserver.server.temporal.records;

public record CsvImportRequest(
        String statementName,
        StoredUploadRef upload
) {
}
