package com.example.springserver.server;

import com.example.springserver.server.dto.UploadCsvInputDto;
import com.example.springserver.server.dto.UploadCsvJobLaunchResult;
import com.example.springserver.server.jobs.OtherIncomeBatchConfig;
import com.example.springserver.server.jobs.SellsBatchConfig;
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
    private final SellsBatchConfig sellsBatchConfig;
    private final OtherIncomeBatchConfig otherIncomeBatchConfig;

    public UploadCsvJobLaunchResult uploadCsv(UploadCsvInputDto input) {
        try {
            byte[] csvBytes = input.getFile().getBytes();
            long launchId = System.currentTimeMillis();

            Job sellsJob = sellsBatchConfig.processSellsCsvJob(csvBytes);
            JobParameters sellsParams = buildJobParameters(input.getName(), launchId);
            var sellsExecution = jobLauncher.run(sellsJob, sellsParams);

            Job otherIncomeJob = otherIncomeBatchConfig.processOtherIncomeCsvJob(csvBytes);
            JobParameters otherIncomeParams = buildJobParameters(input.getName(), launchId);
            var otherIncomeExecution = jobLauncher.run(otherIncomeJob, otherIncomeParams);

            return new UploadCsvJobLaunchResult(sellsExecution.getId(), otherIncomeExecution.getId());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to launch batch job", e);
        }
    }

    private JobParameters buildJobParameters(String name, long launchId) {
        return new JobParametersBuilder()
                .addString("name", name)
                .addLong("timestamp", launchId)
                .toJobParameters();
    }
}
