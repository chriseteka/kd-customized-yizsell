package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;

import java.util.List;


/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
public interface WarehouseServices {

    Warehouse createWarehouse(Long businessOwnerId, Warehouse warehouse);

    Warehouse updateWarehouse(Long warehouseId, Warehouse warehouseUpdates);

    Warehouse warehouseById(Long warehouseId);

    List<Warehouse> fetchAllWarehouse();

    Warehouse deleteWarehouse(Long warehouseId);

    Warehouse fetchWarehouseByWarehouseAttendant(String warehouseAttendantName);

    List<Warehouse> deleteWarehouses(Long... warehouseIds);
}
