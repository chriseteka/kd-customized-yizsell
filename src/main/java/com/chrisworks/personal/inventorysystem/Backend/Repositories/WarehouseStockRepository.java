package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Stock;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.WarehouseStocks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/24/2019
 * @email chriseteka@gmail.com
 */
public interface WarehouseStockRepository extends JpaRepository<WarehouseStocks, Long> {

    List<WarehouseStocks> findAllByWarehouseAndApprovedIsFalse(Warehouse warehouse);

    List<WarehouseStocks> findAllByWarehouseAndApprovedIsTrue(Warehouse warehouse);

    List<WarehouseStocks> findAllByWarehouseAndExpiryDateLessThanEqual(Warehouse warehouse, Date expireBefore);

    WarehouseStocks findDistinctByStockNameAndWarehouse(String stockName, Warehouse warehouse);

    WarehouseStocks findDistinctByStockNameAndWarehouseAndStockQuantityRemainingLessThanEqual
            (String stockName, Warehouse warehouse, int stockRemainingLimit);

    List<WarehouseStocks> findAllByWarehouse(Warehouse warehouse);

    List<WarehouseStocks> findAllByWarehouseAndStockQuantityRemainingIsLessThanEqual(Warehouse warehouse, int limit);

    List<WarehouseStocks> findAllByCreatedByAndApprovedIsFalse(String createdBy);
}
