package com.example.springserver.service;

import com.example.springserver.temporal.records.StoredUploadRef;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class UploadStorageService {

    private final Path storageDirectory;

    public UploadStorageService(@Value("${app.upload.storage-dir:${java.io.tmpdir}/temporal-io-demo/uploads}") String storageDir) {
        this.storageDirectory = Path.of(storageDir).toAbsolutePath().normalize();
    }

    public StoredUploadRef store(MultipartFile file) throws IOException {
        Files.createDirectories(storageDirectory);

        String uploadId = UUID.randomUUID().toString();
        String originalFilename = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "upload.csv";
        String safeFilename = Path.of(originalFilename).getFileName().toString();
        String storedFilename = uploadId + "-" + safeFilename;
        Path destination = storageDirectory.resolve(storedFilename);

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        return new StoredUploadRef(uploadId, destination.toString(), safeFilename);
    }
}
