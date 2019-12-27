package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Invoice;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ReturnedStock;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ShopStocks;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/26/2019
 * @email chriseteka@gmail.com
 */
public interface ShopStockServices {

    //Create and then add stock to shop using the shop id passed
    ShopStocks createStockInShop(Long shopId, ShopStocks stock);

    //Fetch all stock using authenticated user's details
    List<ShopStocks> allStockByShopId(Long shopId);

    //Fetch all stock that will soon finish
    List<ShopStocks> allSoonToFinishStock(Long shopId, int limit);

    //Fetch all stock that will soon expire
    List<ShopStocks> allSoonToExpireStock(Long shopId, Date expiryDateInterval);

    //Fetch all approved stock for seller to sell
    List<ShopStocks> allApprovedStock(Long shopId);

    //Fetch all unapproved stock
    List<ShopStocks> allUnApprovedStock(Long shopId);

    List<ShopStocks> allUnApprovedStockByCreator(String createdBy);

    ShopStocks approveStock(Long stockId);

    List<ShopStocks> approveStockList(List<Long> stockIdList);

    ShopStocks deleteStock(Long stockId);

    ShopStocks reStockToShop(Long shopId, Long stockId, ShopStocks newStock);

    ShopStocks addStockToShop(ShopStocks stockToAdd, Shop shop);

    ShopStocks changeStockSellingPriceByStockId(Long stockId, BigDecimal newSellingPrice);

    ShopStocks changeStockSellingPriceByShopIdAndStockName(Long shopId, String stockName, BigDecimal newSellingPrice);

    Invoice sellStock(Long shopId, Invoice invoice);

    ReturnedStock processReturn(Long shopId, ReturnedStock returnedStock);

    List<ReturnedStock> processReturnList(Long shopId, List<ReturnedStock> returnedStockList);
}
