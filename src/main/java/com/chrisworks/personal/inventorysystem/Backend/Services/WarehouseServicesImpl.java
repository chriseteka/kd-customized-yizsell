package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.BusinessOwnerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ShopRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.WarehouseRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
@Service
public class WarehouseServicesImpl implements WarehouseServices {

    private WarehouseRepository warehouseRepository;

    private BusinessOwnerRepository businessOwnerRepository;

    @Autowired
    public WarehouseServicesImpl(WarehouseRepository warehouseRepository, BusinessOwnerRepository businessOwnerRepository) {
        this.warehouseRepository = warehouseRepository;
        this.businessOwnerRepository = businessOwnerRepository;
    }

//    @Override
//    public Warehouse addShopToWarehouse(Long warehouseId, Shop newShop) {
//
//        AtomicReference<Warehouse> updatedWarehouse = new AtomicReference<>();
//
//        warehouseRepository.findById(warehouseId).ifPresent(warehouse -> {
//
//            Set<Shop> allShops = warehouse.getShops();
//            allShops.add(newShop);
//            warehouse.setShops(allShops);
//            warehouse.setUpdateDate(new Date());
//            updatedWarehouse.set(warehouseRepository.save(warehouse));
//        });
//        return updatedWarehouse.get();
//    }
//
//    @Override
//    public Warehouse addShopListToWarehouse(Long warehouseId, List<Shop> shopList) {
//
//        AtomicReference<Warehouse> updatedWarehouse = new AtomicReference<>();
//
//        warehouseRepository.findById(warehouseId).ifPresent(warehouse -> {
//
//            Set<Shop> allShops = warehouse.getShops();
//            allShops.addAll(shopList);
//            warehouse.setShops(allShops);
//            warehouse.setUpdateDate(new Date());
//            updatedWarehouse.set(warehouseRepository.save(warehouse));
//        });
//
//        return updatedWarehouse.get();
//    }

    @Override
    public Warehouse updateWarehouse(Long warehouseId, Warehouse warehouseUpdates) {

        AtomicReference<Warehouse> updatedWarehouse = new AtomicReference<>();

        warehouseRepository.findById(warehouseId).ifPresent(warehouse -> {

            warehouse.setUpdateDate(new Date());
            warehouse.setWarehouseAddress(warehouseUpdates.getWarehouseAddress());
            updatedWarehouse.set(warehouseRepository.save(warehouse));
        });

        return updatedWarehouse.get();
    }

    @Override
    public Warehouse warehouseById(Long warehouseId) {

        return warehouseRepository.findById(warehouseId).orElse(null);
    }

    @Override
    public Warehouse deleteWarehouse(Warehouse warehouseToDelete) {

        warehouseRepository.delete(warehouseToDelete);

        return warehouseToDelete;
    }

    @Override
    public Warehouse addWarehouse(Long businessOwnerId, Warehouse warehouse) {

        if (null == businessOwnerId || businessOwnerId < 0 || !businessOwnerId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("business owner id error", "business owner id is empty or not a valid number", null);

        if (!businessOwnerId.equals(AuthenticatedUserDetails.getUserId())) throw new InventoryAPIOperationException
                ("business owner id error", "Authenticated id does not match id used for this request", null);

        if (warehouseRepository.findDistinctByWarehouseName(warehouse.getWarehouseName()) != null) throw new
                InventoryAPIOperationException("Warehouse name already exist",
                "A warehouse exist with the warehouse name: " + warehouse.getWarehouseName(), null);

        return businessOwnerRepository.findById(businessOwnerId)
                .map(businessOwner -> {
                    warehouse.setBusinessOwner(businessOwner);
                    return warehouseRepository.save(warehouse);
                }).orElse(null);
    }
}
