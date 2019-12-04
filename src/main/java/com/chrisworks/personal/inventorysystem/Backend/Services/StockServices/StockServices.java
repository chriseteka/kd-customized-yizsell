package com.chrisworks.personal.inventorysystem.Backend.Services.StockServices;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Stock;
import com.chrisworks.personal.inventorysystem.Backend.Services.CRUDServices.CRUDServices;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
public interface StockServices extends CRUDServices<Stock> {

    //Fetch all stock using authenticated user's details
    //Fetch all stock that will soon finish
    //Fetch all stock that will soon expire
    //Fetch all approved stock
    //Fetch all unapproved stock

    Boolean approveStock(Long stockId);

    Boolean approveStockList(List<Long> stockIdList);

}
