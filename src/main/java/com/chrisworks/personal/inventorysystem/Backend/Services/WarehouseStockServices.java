package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.BulkUploadResponseWrapper;
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

    WarehouseStocks createStockInWarehouse(Long warehouseId, WarehouseStocks stock);

    BulkUploadResponseWrapper createStockListInWarehouse(Long warehouseId, List<WarehouseStocks> stocksList);

    List<WarehouseStocks> allStockByWarehouseId(Long warehouseId);

    List<WarehouseStocks> allSoonToFinishStock(Long warehouseId, int limit);

    List<WarehouseStocks> allSoonToExpireStock(Long warehouseId, Date expiryDateInterval);

    List<WarehouseStocks> allApprovedStock(Long warehouseId);

    List<WarehouseStocks> allUnApprovedStock(Long warehouseId);

    List<WarehouseStocks> allUnApprovedStockByCreator(String createdBy);

    WarehouseStocks approveStock(Long stockId);

    List<WarehouseStocks> approveStockList(List<Long> stockIdList);

    WarehouseStocks deleteStock(Long stockId);

    WarehouseStocks reStockToWarehouse(Long warehouseId, Long stockId, WarehouseStocks newStock);

    WarehouseStocks changeStockSellingPriceByStockId(Long stockId, BigDecimal newSellingPrice);

    WarehouseStocks changeStockSellingPriceByWarehouseIdAndStockName(Long warehouseId, String stockName, BigDecimal newSellingPrice);
}
