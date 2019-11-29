package com.chrisworks.personal.inventorysystem.Backend.Services.CRUDServices.CRUDServicesImplementation;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ShopRepository;
import com.chrisworks.personal.inventorysystem.Backend.Services.CRUDServices.CRUDServices;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ShopCRUDImpl implements CRUDServices<Shop> {

    private ShopRepository shopRepository;

    @Autowired
    public ShopCRUDImpl(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    @Override
    public Shop createEntity(Shop shop) {

//        shop.set
//        shopRepository.
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
