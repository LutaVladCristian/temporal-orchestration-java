package com.example.BackEndServer.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "other_income_fees")
public class OtherIncomeFees {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_aquired", nullable = false)
    private LocalDate date;

    @Column(name = "symbol", nullable = false, length = 10)
    private String symbol;

    @Column(name = "security_name", nullable = false)
    private String securityName;

    @Column(name = "isin", nullable = false, length = 20)
    private String isin;

    @Column(name = "country", nullable = false, length = 50)
    private String country;

    @Column(name = "gross_amount", nullable = false)
    private BigDecimal grossAmount;

    @Column(name = "withholding_tax", nullable = false, length = 20)
    private String withholdingTax;

    @Column(name = "net_amount", nullable = false, length = 20)
    private String netAmount;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;
}
