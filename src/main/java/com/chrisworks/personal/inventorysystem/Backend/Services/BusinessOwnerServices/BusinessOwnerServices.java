package com.chrisworks.personal.inventorysystem.Backend.Services.BusinessOwnerServices;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
public interface BusinessOwnerServices{

    //Services peculiar to business owner
    BusinessOwner createAccount(BusinessOwner businessOwner);

    BusinessOwner updateAccount(Long businessOwnerId, BusinessOwner updates);

    BusinessOwner fetchBusinessOwner(Long id);

    Seller createSeller(Seller seller);

    Seller fetchSellerById(Long sellerId);

    Seller fetchSellerByName(String sellerName);

    List<Seller> allSellers();

    Seller deleteSeller(Long sellerId);

    Warehouse addWarehouse(Warehouse warehouse);

    Shop addShop(Shop shop);

    Seller updateSeller(Long sellerId, Seller sellerUpdates);

    Warehouse updateWarehouse(Long warehouseId, Warehouse warehouseUpdates);

    Shop updateShop(Long shopId, Shop shopUpdates);

    Boolean approveStock(String stockName);

    Boolean approveStockList(List<String> stockNameList);

    Boolean approveIncome(Long incomeId);

    Boolean approveExpense(Long expenseId);

    Boolean approveReturn(Long returnedStockId);

    Warehouse addShopToWarehouse(Long warehouseId, Shop shop);

    Warehouse addShopListToWarehouse(Long warehouseId, List<Shop> shopList);

    Shop addSellerToShop(Long shopId, Seller seller);

    Shop addSellerListToShop(Long shopId, List<Seller> sellerList);

    Shop removeSellerFromShop(Long shopId, Seller seller);

    Shop removeSellersFromShop(Long shopId, List<Seller> sellerList);
}
