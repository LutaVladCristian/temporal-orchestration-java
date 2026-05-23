package com.example.springserver.temporal.records;

public record StoredUploadRef(
        String uploadId,
        String storagePath,
        String originalFilename
) {
}
