package com.example.springserver.server.temporal.records;

public record StoredUploadRef(
        String uploadId,
        String storagePath,
        String originalFilename
) {
}
