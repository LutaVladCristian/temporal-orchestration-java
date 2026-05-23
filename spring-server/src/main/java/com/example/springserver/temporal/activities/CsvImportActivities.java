package com.example.springserver.temporal.activities;

import com.example.springserver.temporal.records.CsvImportRequest;
import com.example.springserver.temporal.records.ImportStepResult;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface CsvImportActivities {

    ImportStepResult importSells(CsvImportRequest request);

    ImportStepResult importOtherIncome(CsvImportRequest request);
}
