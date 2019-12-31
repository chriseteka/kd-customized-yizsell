package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockCategory;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/5/2019
 * @email chriseteka@gmail.com
 */
public interface StockCategoryServices extends CRUDServices<StockCategory> {

    StockCategory fetchStockCategoryByName(String stockCategoryName);

    List<StockCategory> fetchAllStockCategoryByCreatedBy(String createdBy);
}
