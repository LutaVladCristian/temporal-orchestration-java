package com.example.BackEndServer.server;

import com.example.BackEndServer.server.dto.UploadCsvInputDto;
import com.example.BackEndServer.server.jobs.CsvBatchConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UploadCsvService {

    private final JobLauncher jobLauncher;
    private final CsvBatchConfig csvBatchConfig;

    public long uploadCsv(UploadCsvInputDto input) {
        try {
            byte[] csvBytes = input.getFile().getBytes();
            Job job = csvBatchConfig.processCsvJob(csvBytes);
            JobParameters params = new JobParametersBuilder()
                    .addString("name", input.getName())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            var execution = jobLauncher.run(job, params);
            return execution.getId();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to launch batch job", e);
        }
    }
}
