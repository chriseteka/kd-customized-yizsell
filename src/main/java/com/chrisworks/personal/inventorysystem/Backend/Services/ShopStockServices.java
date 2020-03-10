package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model.ResponseObject;
import com.chrisworks.personal.inventorysystem.Backend.Entities.BulkUploadResponseWrapper;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.Entities.UniqueStock;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/26/2019
 * @email chriseteka@gmail.com
 */
public interface ShopStockServices {

    ShopStocks createStockInShop(Long shopId, ShopStocks stock);

    BulkUploadResponseWrapper createStockListInShop(Long shopId, List<ShopStocks> stocksList);

    List<ShopStocks> allStockByShopId(Long shopId);

    List<ShopStocks> allSoonToFinishStock(Long shopId, int limit);

    List<ShopStocks> allSoonToExpireStock(Long shopId, Date expiryDateInterval);

    List<ShopStocks> allApprovedStock(Long shopId);

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

    ResponseObject reverseSale(Long shopId, String invoiceNumber);

    ReturnedStock processReturn(Long shopId, ReturnedStock returnedStock);

    List<ReturnedStock> processReturnList(Long shopId, List<ReturnedStock> returnedStockList);

    Invoice processExchange(Long shopId, ReturnedStock returnedStock, ExchangedStock receivedStock);

    ResponseObject processExchangeList(Long shopId, List<ReturnedStock> returnedStockList, List<ExchangedStock> receivedStockList);

    List<UniqueStock> fetchAllAuthUserUniqueStocks(Long shopId);
}
