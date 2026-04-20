package com.example.BackEndServer.server.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomeFromSellsDto {

    private LocalDate dateAcquired;
    private LocalDate dateSold;
    private String symbol;
    private String securityName;
    private String isin;
    private String country;
    private BigDecimal quantity;
    private BigDecimal costBasis;
    private BigDecimal grossProceeds;
    private BigDecimal grossPnl;
    private String currency;
}
