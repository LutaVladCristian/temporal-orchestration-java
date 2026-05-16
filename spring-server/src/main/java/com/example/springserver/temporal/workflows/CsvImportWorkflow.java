package com.example.springserver.temporal.workflows;

import com.example.springserver.temporal.records.CsvImportRequest;
import com.example.springserver.temporal.records.ImportStatusSnapshot;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface CsvImportWorkflow {

    @WorkflowMethod
    ImportStatusSnapshot runImport(CsvImportRequest request);

    @QueryMethod
    ImportStatusSnapshot getStatus();
}
