package com.example.springserver.server.jobs;

import com.example.springserver.server.entity.OtherIncomeFees;
import com.example.springserver.server.repository.OtherIncomeFeesRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class OtherIncomeBatchConfig {

    private static final String OTHER_INCOME_SECTION_HEADER = "Other income & fees";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final OtherIncomeFeesRepository otherIncomeFeesRepository;
    private final CsvSectionExtractor csvSectionExtractor;

    public FlatFileItemReader<OtherIncomeRow> otherIncomeReader(byte[] csvBytes) {
        String section = csvSectionExtractor.extractSection(csvBytes, OTHER_INCOME_SECTION_HEADER);
        return new FlatFileItemReaderBuilder<OtherIncomeRow>()
                .name("otherIncomeReader")
                .resource(new ByteArrayResource(section.getBytes(StandardCharsets.UTF_8)))
                .linesToSkip(1)
                .delimited()
                .names("date", "symbol", "securityName", "isin", "country",
                        "grossAmount", "withholdingTax", "netAmount", "currency")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{ setTargetType(OtherIncomeRow.class); }})
                .build();
    }

    @Bean
    public ItemProcessor<OtherIncomeRow, OtherIncomeFees> otherIncomeProcessor() {
        return row -> OtherIncomeFees.builder()
                .date(LocalDate.parse(row.getDate()))
                .symbol(row.getSymbol())
                .securityName(row.getSecurityName())
                .isin(row.getIsin())
                .country(row.getCountry())
                .grossAmount(new BigDecimal(row.getGrossAmount()))
                .withholdingTax(row.getWithholdingTax())
                .netAmount(row.getNetAmount())
                .currency(row.getCurrency())
                .build();
    }

    @Bean
    public RepositoryItemWriter<OtherIncomeFees> otherIncomeWriter() {
        return new RepositoryItemWriterBuilder<OtherIncomeFees>()
                .repository(otherIncomeFeesRepository)
                .methodName("save")
                .build();
    }

    public Step otherIncomeStep(byte[] csvBytes) {
        return new StepBuilder("otherIncomeStep", jobRepository)
                .<OtherIncomeRow, OtherIncomeFees>chunk(10, transactionManager)
                .reader(otherIncomeReader(csvBytes))
                .processor(otherIncomeProcessor())
                .writer(otherIncomeWriter())
                .build();
    }

    public Job processOtherIncomeCsvJob(byte[] csvBytes) {
        return new JobBuilder("processOtherIncomeCsvJob", jobRepository)
                .start(otherIncomeStep(csvBytes))
                .build();
    }

    @Setter
    @Getter
    public static class OtherIncomeRow {
        private String date, symbol, securityName, isin, country,
                grossAmount, withholdingTax, netAmount, currency;
    }
}
