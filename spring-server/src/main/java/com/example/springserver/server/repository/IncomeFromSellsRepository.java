package com.example.springserver.server.repository;

import com.example.springserver.server.entity.IncomeFromSells;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncomeFromSellsRepository extends JpaRepository<IncomeFromSells, Long> {
}
