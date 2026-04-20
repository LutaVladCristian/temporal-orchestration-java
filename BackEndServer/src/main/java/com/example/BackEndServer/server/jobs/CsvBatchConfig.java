package com.example.BackEndServer.server.jobs;

import com.example.BackEndServer.server.entity.IncomeFromSells;
import com.example.BackEndServer.server.entity.OtherIncomeFees;
import com.example.BackEndServer.server.repository.IncomeFromSellsRepository;
import com.example.BackEndServer.server.repository.OtherIncomeFeesRepository;
import lombok.RequiredArgsConstructor;
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
public class CsvBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final IncomeFromSellsRepository incomeFromSellsRepository;
    private final OtherIncomeFeesRepository otherIncomeFeesRepository;

    // ── Sells step ────────────────────────────────────────────────────────────

    public FlatFileItemReader<SellsRow> sellsReader(byte[] csvBytes) {
        String sellsSection = extractSection(new String(csvBytes, StandardCharsets.UTF_8), "Income from Sells");
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

    // ── Other income step ─────────────────────────────────────────────────────

    public FlatFileItemReader<OtherIncomeRow> otherIncomeReader(byte[] csvBytes) {
        String section = extractSection(new String(csvBytes, StandardCharsets.UTF_8), "Other income & fees");
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

    // ── Job ───────────────────────────────────────────────────────────────────

    public Job processCsvJob(byte[] csvBytes) {
        return new JobBuilder("processCsvJob", jobRepository)
                .start(sellsStep(csvBytes))
                .next(otherIncomeStep(csvBytes))
                .build();
    }

    // ── CSV section splitter ──────────────────────────────────────────────────

    private String extractSection(String csv, String sectionHeader) {
        String[] lines = csv.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();
        boolean inSection = false;
        for (String line : lines) {
            if (line.trim().equals(sectionHeader)) {
                inSection = true;
                continue;
            }
            if (inSection) {
                if (line.isBlank()) break;
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    // ── Raw row POJOs (flat file mapper targets) ──────────────────────────────

    public static class SellsRow {
        private String dateAcquired, dateSold, symbol, securityName, isin, country,
                quantity, costBasis, grossProceeds, grossPnl, currency;

        public String getDateAcquired() { return dateAcquired; }
        public void setDateAcquired(String v) { this.dateAcquired = v; }
        public String getDateSold() { return dateSold; }
        public void setDateSold(String v) { this.dateSold = v; }
        public String getSymbol() { return symbol; }
        public void setSymbol(String v) { this.symbol = v; }
        public String getSecurityName() { return securityName; }
        public void setSecurityName(String v) { this.securityName = v; }
        public String getIsin() { return isin; }
        public void setIsin(String v) { this.isin = v; }
        public String getCountry() { return country; }
        public void setCountry(String v) { this.country = v; }
        public String getQuantity() { return quantity; }
        public void setQuantity(String v) { this.quantity = v; }
        public String getCostBasis() { return costBasis; }
        public void setCostBasis(String v) { this.costBasis = v; }
        public String getGrossProceeds() { return grossProceeds; }
        public void setGrossProceeds(String v) { this.grossProceeds = v; }
        public String getGrossPnl() { return grossPnl; }
        public void setGrossPnl(String v) { this.grossPnl = v; }
        public String getCurrency() { return currency; }
        public void setCurrency(String v) { this.currency = v; }
    }

    public static class OtherIncomeRow {
        private String date, symbol, securityName, isin, country,
                grossAmount, withholdingTax, netAmount, currency;

        public String getDate() { return date; }
        public void setDate(String v) { this.date = v; }
        public String getSymbol() { return symbol; }
        public void setSymbol(String v) { this.symbol = v; }
        public String getSecurityName() { return securityName; }
        public void setSecurityName(String v) { this.securityName = v; }
        public String getIsin() { return isin; }
        public void setIsin(String v) { this.isin = v; }
        public String getCountry() { return country; }
        public void setCountry(String v) { this.country = v; }
        public String getGrossAmount() { return grossAmount; }
        public void setGrossAmount(String v) { this.grossAmount = v; }
        public String getWithholdingTax() { return withholdingTax; }
        public void setWithholdingTax(String v) { this.withholdingTax = v; }
        public String getNetAmount() { return netAmount; }
        public void setNetAmount(String v) { this.netAmount = v; }
        public String getCurrency() { return currency; }
        public void setCurrency(String v) { this.currency = v; }
    }
}
