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
public class OtherIncomeFeesDto {

    private LocalDate date;
    private String symbol;
    private String securityName;
    private String isin;
    private String country;
    private BigDecimal grossAmount;
    // Raw strings because the CSV mixes currency symbols into values (e.g. "0.04 RON", "$0")
    private String withholdingTax;
    private String netAmount;
    private String currency;
}
