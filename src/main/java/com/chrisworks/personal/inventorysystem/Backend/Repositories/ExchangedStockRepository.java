package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ExchangedStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangedStockRepository extends JpaRepository<ExchangedStock, Long> {
}
