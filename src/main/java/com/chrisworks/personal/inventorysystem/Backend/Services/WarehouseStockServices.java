package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.WarehouseStocks;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/24/2019
 * @email chriseteka@gmail.com
 */
public interface WarehouseStockServices {

    //Create and then add stock to warehouse using the warehouse id passed
    WarehouseStocks createStockInWarehouse(Long warehouseId, WarehouseStocks stock);

    //Fetch all stock using authenticated user's details
    List<WarehouseStocks> allStockByWarehouseId(Long warehouseId);

    //Fetch all stock that will soon finish
    List<WarehouseStocks> allSoonToFinishStock(Long warehouseId, int limit);

    //Fetch all stock that will soon expire
    List<WarehouseStocks> allSoonToExpireStock(Long warehouseId, Date expiryDateInterval);

    //Fetch all approved stock for seller to sell
    List<WarehouseStocks> allApprovedStock(Long warehouseId);

    //Fetch all unapproved stock
    List<WarehouseStocks> allUnApprovedStock(Long warehouseId);

    List<WarehouseStocks> allUnApprovedStockByCreator(String createdBy);

    WarehouseStocks approveStock(Long stockId);

    List<WarehouseStocks> approveStockList(List<Long> stockIdList);

    WarehouseStocks deleteStock(Long stockId);

    WarehouseStocks reStockToWarehouse(Long warehouseId, Long stockId, WarehouseStocks newStock);

    WarehouseStocks changeStockSellingPriceByStockId(Long stockId, BigDecimal newSellingPrice);

    WarehouseStocks changeStockSellingPriceByWarehouseIdAndStockName(Long warehouseId, String stockName, BigDecimal newSellingPrice);
}
