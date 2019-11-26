package com.chrisworks.personal.inventorysystembackend.Backend.Services.BusinessOwnerServices;

import com.chrisworks.personal.inventorysystembackend.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystembackend.Backend.Services.GenericServices.GenericService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
@Service
@Transactional
public interface BusinessOwnerServices extends GenericService<BusinessOwner> {

    //Services peculiar to business owner
    BusinessOwner createAccount(BusinessOwner businessOwner);

    Seller createSeller(Seller seller);

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
}
