package com.example.springserver.server.jobs;

import com.example.springserver.server.entity.IncomeFromSells;
import com.example.springserver.server.repository.IncomeFromSellsRepository;
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
public class SellsBatchConfig {

    private static final String SELLS_SECTION_HEADER = "Income from Sells";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final IncomeFromSellsRepository incomeFromSellsRepository;
    private final CsvSectionExtractor csvSectionExtractor;

    public FlatFileItemReader<SellsRow> sellsReader(byte[] csvBytes) {
        String sellsSection = csvSectionExtractor.extractSection(csvBytes, SELLS_SECTION_HEADER);
        return new FlatFileItemReaderBuilder<SellsRow>()
                .name("sellsReader")
                .resource(new ByteArrayResource(sellsSection.getBytes(StandardCharsets.UTF_8)))
                .linesToSkip(1)
                .delimited()
                .names("dateAcquired", "dateSold", "symbol", "securityName", "isin", "country",
                        "quantity", "costBasis", "grossProceeds", "grossPnl", "currency")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{ setTargetType(SellsRow.class); }})
                .build();
    }

    @Bean
    public ItemProcessor<SellsRow, IncomeFromSells> sellsProcessor() {
        return row -> IncomeFromSells.builder()
                .dateAcquired(LocalDate.parse(row.getDateAcquired()))
                .dateSold(LocalDate.parse(row.getDateSold()))
                .symbol(row.getSymbol())
                .securityName(row.getSecurityName())
                .isin(row.getIsin())
                .country(row.getCountry())
                .quantity(new BigDecimal(row.getQuantity()))
                .costBasis(new BigDecimal(row.getCostBasis()))
                .grossProceeds(new BigDecimal(row.getGrossProceeds()))
                .grossPnl(new BigDecimal(row.getGrossPnl()))
                .currency(row.getCurrency())
                .build();
    }

    @Bean
    public RepositoryItemWriter<IncomeFromSells> sellsWriter() {
        return new RepositoryItemWriterBuilder<IncomeFromSells>()
                .repository(incomeFromSellsRepository)
                .methodName("save")
                .build();
    }

    public Step sellsStep(byte[] csvBytes) {
        return new StepBuilder("sellsStep", jobRepository)
                .<SellsRow, IncomeFromSells>chunk(10, transactionManager)
                .reader(sellsReader(csvBytes))
                .processor(sellsProcessor())
                .writer(sellsWriter())
                .build();
    }

    public Job processSellsCsvJob(byte[] csvBytes) {
        return new JobBuilder("processSellsCsvJob", jobRepository)
                .start(sellsStep(csvBytes))
                .build();
    }

    @Setter
    @Getter
    public static class SellsRow {
        private String dateAcquired, dateSold, symbol, securityName, isin, country,
                quantity, costBasis, grossProceeds, grossPnl, currency;
    }
}
