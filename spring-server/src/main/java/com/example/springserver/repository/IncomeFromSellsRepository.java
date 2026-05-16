package com.example.springserver.repository;

import com.example.springserver.entity.IncomeFromSells;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncomeFromSellsRepository extends JpaRepository<IncomeFromSells, Long> {
}
