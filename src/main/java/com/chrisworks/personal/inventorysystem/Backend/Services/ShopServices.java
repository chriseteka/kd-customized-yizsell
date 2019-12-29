package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.Services.CRUDServices;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
public interface ShopServices {

    Shop createShop(Long businessOwnerId, Shop shop);

    Shop findShopById(Long shopId);

    Shop updateShop(Long shopId, Shop shopUpdates);

    List<Shop> fetchAllShops();

    Shop deleteShop(Long shopId);
}
