package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.BusinessOwnerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.WarehouseRepository;
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
    public Warehouse addWarehouse(Long businessOwnerId, Warehouse warehouse) {

        return businessOwnerRepository.findById(businessOwnerId)
                .map(businessOwner -> {
                    warehouse.setBusinessOwner(businessOwner);
                    return warehouseRepository.save(warehouse);
                }).orElse(null);
    }
}
