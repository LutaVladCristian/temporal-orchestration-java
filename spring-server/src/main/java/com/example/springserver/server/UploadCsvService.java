package com.example.springserver.server;

import com.example.springserver.server.dto.ImportStatusResponseDto;
import com.example.springserver.server.dto.ImportStepStatusDto;
import com.example.springserver.server.dto.UploadCsvInputDto;
import com.example.springserver.server.dto.UploadCsvResponseDto;
import com.example.springserver.server.temporal.records.CsvImportRequest;
import com.example.springserver.server.temporal.workflows.CsvImportWorkflow;
import com.example.springserver.server.temporal.records.ImportStatusSnapshot;
import com.example.springserver.server.temporal.TemporalTaskQueues;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowNotFoundException;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UploadCsvService {

    private final WorkflowClient workflowClient;
    private final UploadStorageService uploadStorageService;

    public UploadCsvResponseDto uploadCsv(UploadCsvInputDto input) {
        try {
            var storedUpload = uploadStorageService.store(input.getFile());
            String workflowId = "csv-import-" + storedUpload.uploadId();

            CsvImportWorkflow workflow = workflowClient.newWorkflowStub(
                    CsvImportWorkflow.class,
                    WorkflowOptions.newBuilder()
                            .setTaskQueue(TemporalTaskQueues.CSV_IMPORT_TASK_QUEUE)
                            .setWorkflowId(workflowId)
                            .build());

            CsvImportRequest request = new CsvImportRequest(input.getName(), storedUpload);
            var execution = WorkflowClient.start(workflow::runImport, request);

            return new UploadCsvResponseDto(execution.getWorkflowId(), execution.getRunId(), input.getName());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to start Temporal workflow", e);
        }
    }

    public ImportStatusResponseDto getImportStatus(String workflowId) {
        try {
            CsvImportWorkflow workflow = workflowClient.newWorkflowStub(CsvImportWorkflow.class, workflowId);
            ImportStatusSnapshot snapshot = workflow.getStatus();
            if (snapshot == null) {
                return null;
            }

            return new ImportStatusResponseDto(
                    snapshot.workflowId(),
                    snapshot.statementName(),
                    snapshot.status(),
                    snapshot.createdAt(),
                    snapshot.startedAt(),
                    snapshot.completedAt(),
                    snapshot.steps().stream()
                            .map(step -> new ImportStepStatusDto(
                                    step.stepName(),
                                    step.status(),
                                    step.readCount(),
                                    step.writeCount()))
                            .toList(),
                    snapshot.failureMessages()
            );
        } catch (WorkflowNotFoundException e) {
            return null;
        }
    }
}
