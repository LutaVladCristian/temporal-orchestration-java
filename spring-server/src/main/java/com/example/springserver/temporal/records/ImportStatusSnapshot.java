package com.example.springserver.temporal.records;

import java.util.ArrayList;
import java.util.List;

public record ImportStatusSnapshot(
        String workflowId,
        String statementName,
        String status,
        String createdAt,
        String startedAt,
        String completedAt,
        List<ImportStepSnapshot> steps,
        List<String> failureMessages
) {

    public ImportStatusSnapshot withStepResult(ImportStepResult result) {
        List<ImportStepSnapshot> nextSteps = new ArrayList<>(steps.size());
        for (ImportStepSnapshot step : steps) {
            if (step.stepName().equals(result.stepName())) {
                nextSteps.add(new ImportStepSnapshot(step.stepName(), "COMPLETED", result.readCount(), result.writeCount()));
            } else {
                nextSteps.add(step);
            }
        }
        return new ImportStatusSnapshot(workflowId, statementName, status, createdAt, startedAt, completedAt, List.copyOf(nextSteps), failureMessages);
    }

    public ImportStatusSnapshot completed(String finishedAt) {
        return new ImportStatusSnapshot(workflowId, statementName, "COMPLETED", createdAt, startedAt, finishedAt, steps, failureMessages);
    }

    public ImportStatusSnapshot failed(String finishedAt, String failureMessage, ImportStepResult sellsResult, ImportStepResult otherIncomeResult) {
        List<ImportStepSnapshot> nextSteps = new ArrayList<>(steps.size());
        for (ImportStepSnapshot step : steps) {
            if (sellsResult != null && step.stepName().equals(sellsResult.stepName())) {
                nextSteps.add(new ImportStepSnapshot(step.stepName(), "COMPLETED", sellsResult.readCount(), sellsResult.writeCount()));
            } else if (otherIncomeResult != null && step.stepName().equals(otherIncomeResult.stepName())) {
                nextSteps.add(new ImportStepSnapshot(step.stepName(), "COMPLETED", otherIncomeResult.readCount(), otherIncomeResult.writeCount()));
            } else {
                nextSteps.add(new ImportStepSnapshot(step.stepName(), "FAILED", step.readCount(), step.writeCount()));
            }
        }

        return new ImportStatusSnapshot(
                workflowId,
                statementName,
                "FAILED",
                createdAt,
                startedAt,
                finishedAt,
                List.copyOf(nextSteps),
                List.of(failureMessage)
        );
    }
}
