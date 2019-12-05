package com.chrisworks.personal.inventorysystem.Backend.Services.StockCategoryServices;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockCategory;
import com.chrisworks.personal.inventorysystem.Backend.Services.CRUDServices.CRUDServices;

/**
 * @author Chris_Eteka
 * @since 12/5/2019
 * @email chriseteka@gmail.com
 */
public interface StockCategoryServices extends CRUDServices<StockCategory> {

    StockCategory findByStockCategoryName(String stockCategoryName);
}
