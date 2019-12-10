package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Stock;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockCategory;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Supplier;
import com.chrisworks.personal.inventorysystem.Backend.Services.CRUDServices;

import java.util.Date;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
public interface StockServices extends CRUDServices<Stock> {

    //Fetch all stock using authenticated user's details
    List<Stock> allStockByWarehouseId(Long warehouseId);

    //Fetch all stock that will soon finish
    List<Stock> allSoonToFinishStock(Long warehouseId, int limit);

    //Fetch all stock that will soon expire
    List<Stock> allSoonToExpireStock(Long warehouseId, Date expiryDateInterval);

    //Fetch all approved stock for seller to sell
    List<Stock> allApprovedStock(Long warehouseId);

    //Fetch all unapproved stock
    List<Stock> unApprovedStock(Long warehouseId);

    List<Stock> unApprovedStockByCreator(String createdBy);

    Stock approveStock(Long stockId);

    List<Stock> approveStockList(List<Long> stockIdList);

    List<Stock> deleteStockList(List<Stock> stockListToDelete);

    StockCategory deleteStockCategory(Long stockCategoryId);

    Supplier deleteSupplier(Long supplierId);
}
