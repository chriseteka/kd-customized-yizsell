package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.WarehouseStocks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/24/2019
 * @email chriseteka@gmail.com
 */
public interface WarehouseStockRepository extends JpaRepository<WarehouseStocks, Long> {

    WarehouseStocks findDistinctByStockNameAndWarehouse(String stockName, Warehouse warehouse);

    List<WarehouseStocks> findAllByWarehouse(Warehouse warehouse);

    List<WarehouseStocks> findAllByCreatedByAndApprovedIsFalse(String createdBy);
}
