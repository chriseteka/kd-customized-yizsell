package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.Services.CRUDServices;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
public interface ShopServices extends CRUDServices<Shop> {

    Shop addShop(Warehouse warehouse, Shop shop);

    Shop addSellerToShop(Shop shop, Seller seller);

    Shop findShopById(Long shopId);

    Income approveIncome(Long incomeId);

    List<Income> allUnApprovedIncome();

    Shop addSellerListToShop(Long shopId, List<Seller> sellerList);

    Shop removeSellerFromShop(Long shopId, Seller seller);

    Shop removeSellersFromShop(Long shopId, List<Seller> sellerList);

    Shop updateShop(Long shopId, Shop shopUpdates);

    List<Shop> fetchAllShopInWarehouse(Long warehouseId);

    Expense approveExpense(Long expenseId);

    List<Expense> allUnApprovedExpense();

    ReturnedStock approveReturnSales(Long returnSaleId);

    List<ReturnedStock> allUnApprovedReturnSales();
}
