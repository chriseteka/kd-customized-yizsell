package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;


/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
public interface WarehouseServices {

    Warehouse addWarehouse(Long businessOwnerId, Warehouse warehouse);

    Warehouse updateWarehouse(Long warehouseId, Warehouse warehouseUpdates);

    Warehouse warehouseById(Long warehouseId);

//    Warehouse addShopToWarehouse(Long warehouseId, Shop shop);
//
//    Warehouse addShopListToWarehouse(Long warehouseId, List<Shop> shopList);

    Warehouse deleteWarehouse(Warehouse warehouseToDelete);
}
