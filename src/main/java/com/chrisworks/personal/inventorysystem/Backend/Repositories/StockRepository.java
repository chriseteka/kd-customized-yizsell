package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Stock;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findAllByWarehousesAndApprovedIsFalse(Warehouse warehouse);

    List<Stock> findAllByWarehousesAndApprovedIsTrue(Warehouse warehouse);

    List<Stock> findAllByWarehousesAndExpiryDateLessThanEqual(Warehouse warehouse, Date expireBefore);

    Stock findDistinctByStockNameAndWarehouses(String stockName, Warehouse warehouse);

    List<Stock> findAllByWarehouses(Warehouse warehouse);

    List<Stock> findAllByWarehousesAndStockQuantityRemainingIsLessThan(Warehouse warehouse, int limit);

    List<Stock> findAllByCreatedByAndAndApprovedIsFalse(String createdBy);
}
