package com.example.springserver.config;

import com.example.springserver.server.temporal.activities.CsvImportActivitiesImpl;
import com.example.springserver.server.temporal.workflows.CsvImportWorkflowImpl;
import com.example.springserver.server.temporal.TemporalTaskQueues;
import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalWorkerConfig {

    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        return WorkerFactory.newInstance(workflowClient);
    }

    @Bean
    public ApplicationRunner temporalWorkerStarter(WorkerFactory workerFactory, CsvImportActivitiesImpl csvImportActivities) {
        return args -> {
            Worker worker = workerFactory.newWorker(TemporalTaskQueues.CSV_IMPORT_TASK_QUEUE);
            worker.registerWorkflowImplementationTypes(CsvImportWorkflowImpl.class);
            worker.registerActivitiesImplementations(csvImportActivities);
            workerFactory.start();
        };
    }
}
