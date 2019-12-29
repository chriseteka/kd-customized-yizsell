package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockSold;

import java.util.Date;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
public interface StockSoldServices {

    List<StockSold> fetchAllStockSoldByAuthenticatedUser();

    List<StockSold> fetchAllStockSoldByInvoiceId(Long invoiceId);

    List<StockSold> fetchAllStockSoldByByInvoiceNumber(String invoiceNumber);

    List<StockSold> fetchAllStockSoldBySellerEmail(String sellerEmail);

    List<StockSold> fetchAllStockSoldByDate(Date date);

    List<StockSold> fetchAllStockSoldByShop(Long shopId);

    List<StockSold> fetchAllStockSoldByStockName(String stockName);

    List<StockSold> fetchAllStockSoldByStockCategory(String stockCategory);

    List<StockSold> fetchAllStockSoldToCustomer(Long customerId);
}
