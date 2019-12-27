package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
public interface SellerServices {

    //Services peculiar to sellers only
    Seller createSeller(Seller seller);

    Seller fetchSellerById(Long sellerId);

    Seller fetchSellerByNameOrEmail(String sellerName);

    List<Seller> allSellersByWarehouseId(Long warehouseId);

    List<Seller> allSellersByShopId(Long shopId);

    Seller updateSeller(Long sellerId, Seller sellerUpdates);

    Seller deleteSeller(Long sellerId);

    List<Seller> deleteSellerList(List<Seller> sellerList);

    List<Seller> fetchSellerByShop(Shop shop);

    List<Seller> fetchSellerByWarehouse(Warehouse warehouse);

    List<Seller> fetchSellers();
}
