package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Stock;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
public interface StockRepository extends JpaRepository<Stock, Long> {

    Stock findDistinctByStockNameAndApprovedIsFalse(String stockName);

    Stock findDistinctByStockNameAndWarehouses(String stockName, Warehouse warehouse);
}
