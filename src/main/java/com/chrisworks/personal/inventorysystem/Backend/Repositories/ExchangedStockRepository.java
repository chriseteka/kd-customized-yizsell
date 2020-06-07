package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ExchangedStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExchangedStockRepository extends JpaRepository<ExchangedStock, Long> {

    List<ExchangedStock> findAllByCustomerId(Customer customer);
}
