package com.example.springserver.server.entity;

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
@Table(name = "income_from_sells")
public class IncomeFromSells {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_aquired", nullable = false)
    private LocalDate dateAcquired;

    @Column(name = "date_sold", nullable = false)
    private LocalDate dateSold;

    @Column(name = "symbol", nullable = false, length = 32)
    private String symbol;

    @Column(name = "security_name", nullable = false)
    private String securityName;

    @Column(name = "isin", nullable = false, length = 20)
    private String isin;

    @Column(name = "country", nullable = false, length = 50)
    private String country;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @Column(name = "cost_basis", nullable = false)
    private BigDecimal costBasis;

    @Column(name = "gross_proceeds", nullable = false)
    private BigDecimal grossProceeds;

    @Column(name = "gross_pnl", nullable = false)
    private BigDecimal grossPnl;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;
}
