package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.BusinessOwnerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ShopRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
@Service
public class ShopServicesImpl implements ShopServices {

    private final ShopRepository shopRepository;

    private final WarehouseRepository warehouseRepository;

    @Autowired
    public ShopServicesImpl(ShopRepository shopRepository, WarehouseRepository warehouseRepository) {
        this.shopRepository = shopRepository;
        this.warehouseRepository = warehouseRepository;
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

        if (null == warehouseId || warehouseId < 0 || !warehouseId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("warehouse id error", "warehouse id is empty or not a valid number", null);

        return warehouseRepository.findById(warehouseId)
                .map(shopRepository::findAllByWarehouses)
                .orElse(Collections.emptyList());

    }

    @Override
    public Shop addShop(Warehouse warehouse, Shop shop) {

        Set<Warehouse> warehouseSet = new HashSet<>();
        warehouseSet.add(warehouse);
        shop.setWarehouses(warehouseSet);
        return shopRepository.save(shop);
    }

    @Override
    public Shop addSellerToShop(Shop shop, Seller seller) {

        if (null == seller) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find seller entity to save", null);

        Set<Seller> allSellers = shop.getSellers();
        allSellers.add(seller);
        shop.setSellers(allSellers);
        shop.setUpdateDate(new Date());

        return shopRepository.save(shop);
    }

    @Override
    public Shop findShopById(Long shopId) {
        return shopRepository.findById(shopId).orElse(null);
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
