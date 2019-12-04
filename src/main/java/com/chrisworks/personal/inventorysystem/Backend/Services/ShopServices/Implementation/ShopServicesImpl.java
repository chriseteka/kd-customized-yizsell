package com.chrisworks.personal.inventorysystem.Backend.Services.ShopServices.Implementation;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.BusinessOwnerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ShopRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.WarehouseRepository;
import com.chrisworks.personal.inventorysystem.Backend.Services.ShopServices.ShopServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
@Service
public class ShopServicesImpl implements ShopServices {

    private final ShopRepository shopRepository;

    private final WarehouseRepository warehouseRepository;

    private final BusinessOwnerRepository businessOwnerRepository;

    @Autowired
    public ShopServicesImpl(ShopRepository shopRepository, WarehouseRepository warehouseRepository,
                            BusinessOwnerRepository businessOwnerRepository) {
        this.shopRepository = shopRepository;
        this.warehouseRepository = warehouseRepository;
        this.businessOwnerRepository = businessOwnerRepository;
    }

    @Override
    public Shop updateShop(Long shopId, Shop shopUpdates) {

        AtomicReference<Shop> updatedShop = new AtomicReference<>();

        shopRepository.findById(shopId).ifPresent(shop -> {

            shop.setUpdateDate(shopUpdates.getUpdateDate());
            shop.setShopAddress(shopUpdates.getShopAddress());
            updatedShop.set(shopRepository.save(shop));
        });

        return updatedShop.get();
    }

    @Override
    public List<Shop> fetchAllShopInWarehouse(Long warehouseId) {

        Optional<Warehouse> warehouse = warehouseRepository.findById(warehouseId);

        return warehouse.map(value -> shopRepository.findAll().stream()
                .filter(shop -> shop.getWarehouses()
                        .stream()
                        .allMatch(warehouse1 -> warehouse1.getWarehouseId().equals(value.getWarehouseId())))
                .collect(Collectors.toList())).orElse(null);

    }

    @Override
    public Shop addShop(Long warehouseId, Shop shop) {

//        return warehouseRepository.findById(warehouseId)
//                .map(warehouse -> {
//
//                    Set<Warehouse> warehouseSet = new HashSet<>();
//                    warehouseSet.add(warehouse);
//                    shop.setWarehouses(warehouseSet);
//                    return shopRepository.save(shop);
//                }).orElse(null);

        Optional<Warehouse> warehouse = warehouseRepository.findById(warehouseId);

        if (warehouse.isPresent()){

            Set<Warehouse> warehouseSet = new HashSet<>();
            warehouseSet.add(warehouse.get());
            shop.setWarehouses(warehouseSet);
            return shopRepository.save(shop);
        }

        return null;
    }

    @Override
    public Shop addSellerToShop(Long shopId, Seller seller) {

        AtomicReference<Shop> updatedShop = new AtomicReference<>();

        shopRepository.findById(shopId).ifPresent(shop -> {

            Set<Seller> allSellers = shop.getSellers();
            allSellers.add(seller);
            shop.setSellers(allSellers);
            shop.setUpdateDate(new Date());
            updatedShop.set(shopRepository.save(shop));
        });

        return updatedShop.get();
    }

    @Override
    public Shop addSellerListToShop(Long shopId, List<Seller> sellerList) {

        AtomicReference<Shop> updatedShop = new AtomicReference<>();

        shopRepository.findById(shopId).ifPresent(shop -> {

            Set<Seller> allSellers = shop.getSellers();
            allSellers.addAll(sellerList);
            shop.setSellers(allSellers);
            shop.setUpdateDate(new Date());
            updatedShop.set(shopRepository.save(shop));
        });

        return updatedShop.get();
    }

    @Override
    public Shop removeSellerFromShop(Long shopId, Seller seller) {

        AtomicReference<Shop> updatedShop = new AtomicReference<>();

        shopRepository.findById(shopId).ifPresent(shop -> {

            Set<Seller> sellerSet = shop.getSellers();
            sellerSet.remove(seller);
            shop.setSellers(sellerSet);
            updatedShop.set(shopRepository.save(shop));
        });

        return updatedShop.get();
    }

    @Override
    public Shop removeSellersFromShop(Long shopId, List<Seller> sellerList) {

        AtomicReference<Shop> updatedShop = new AtomicReference<>();

        shopRepository.findById(shopId).ifPresent(shop -> {

            Set<Seller> sellerSet = shop.getSellers();
            sellerSet.removeAll(sellerList);
            shop.setSellers(sellerSet);
            updatedShop.set(shopRepository.save(shop));
        });

        return updatedShop.get();
    }

    @Override
    public Shop createEntity(Shop shop) {

//        addShop(shop);
        return null;
    }

    @Override
    public Shop updateEntity(Long entityId, Shop shop) {
        return null;
    }

    @Override
    public Shop getSingleEntity(Long entityId) {
        return null;
    }

    @Override
    public List<Shop> getEntityList() {
        return null;
    }

    @Override
    public Shop deleteEntity(Long entityId) {
        return null;
    }
}
