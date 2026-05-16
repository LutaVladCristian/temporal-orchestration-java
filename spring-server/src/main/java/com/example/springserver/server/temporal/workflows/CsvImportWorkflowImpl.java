package com.example.springserver.server.temporal.workflows;

import com.example.springserver.server.temporal.activities.CsvImportActivities;
import com.example.springserver.server.temporal.records.CsvImportRequest;
import com.example.springserver.server.temporal.records.ImportStatusSnapshot;
import com.example.springserver.server.temporal.records.ImportStepResult;
import com.example.springserver.server.temporal.records.ImportStepSnapshot;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class CsvImportWorkflowImpl implements CsvImportWorkflow {

    private final CsvImportActivities activities = Workflow.newActivityStub(
            CsvImportActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(2))
                    .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(1).build())
                    .build());

    private ImportStatusSnapshot status;

    @Override
    public ImportStatusSnapshot runImport(CsvImportRequest request) {
        this.status = new ImportStatusSnapshot(
                Workflow.getInfo().getWorkflowId(),
                request.statementName(),
                "RUNNING",
                now(),
                now(),
                null,
                List.of(
                        new ImportStepSnapshot("sellsStep", "RUNNING", 0, 0),
                        new ImportStepSnapshot("otherIncomeStep", "RUNNING", 0, 0)
                ),
                List.of()
        );

        Promise<ImportStepResult> sellsPromise = Async.function(activities::importSells, request);
        Promise<ImportStepResult> otherIncomePromise = Async.function(activities::importOtherIncome, request);

        boolean sellsDone = false;
        boolean otherIncomeDone = false;
        ImportStepResult sellsResult = null;
        ImportStepResult otherIncomeResult = null;

        try {
            while (!sellsDone || !otherIncomeDone) {
                final boolean waitForSells = !sellsDone;
                final boolean waitForOtherIncome = !otherIncomeDone;
                Workflow.await(() ->
                        (waitForSells && sellsPromise.isCompleted()) ||
                                (waitForOtherIncome && otherIncomePromise.isCompleted()));

                if (!sellsDone && sellsPromise.isCompleted()) {
                    sellsResult = sellsPromise.get();
                    sellsDone = true;
                    status = status.withStepResult(sellsResult);
                }

                if (!otherIncomeDone && otherIncomePromise.isCompleted()) {
                    otherIncomeResult = otherIncomePromise.get();
                    otherIncomeDone = true;
                    status = status.withStepResult(otherIncomeResult);
                }
            }

            status = status.completed(now());
            return status;
        } catch (RuntimeException e) {
            status = status.failed(now(), messageOf(e), sellsResult, otherIncomeResult);
            throw e;
        }
    }

    @Override
    public ImportStatusSnapshot getStatus() {
        return status;
    }

    private String now() {
        return Instant.ofEpochMilli(Workflow.currentTimeMillis()).toString();
    }

    private String messageOf(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : throwable.getClass().getSimpleName();
    }
}
