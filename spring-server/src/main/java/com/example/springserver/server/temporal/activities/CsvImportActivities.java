package com.example.springserver.server.temporal.activities;

import com.example.springserver.server.temporal.records.CsvImportRequest;
import com.example.springserver.server.temporal.records.ImportStepResult;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface CsvImportActivities {

    ImportStepResult importSells(CsvImportRequest request);

    ImportStepResult importOtherIncome(CsvImportRequest request);
}
