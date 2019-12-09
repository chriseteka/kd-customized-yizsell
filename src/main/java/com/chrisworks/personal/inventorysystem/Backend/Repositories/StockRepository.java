package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Stock;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findAllByWarehousesAndApprovedIsFalse(Warehouse warehouse);

    Stock findDistinctByStockNameAndWarehouses(String stockName, Warehouse warehouse);

    List<Stock> findAllByWarehouses(Warehouse warehouse);

    List<Stock> findAllByCreatedByAndAndApprovedIsFalse(String createdBy);
}
