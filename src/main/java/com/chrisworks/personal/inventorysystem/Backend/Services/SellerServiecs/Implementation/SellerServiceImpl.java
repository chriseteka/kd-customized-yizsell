package com.chrisworks.personal.inventorysystem.Backend.Services.SellerServiecs.Implementation;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ShopRepository;
import com.chrisworks.personal.inventorysystem.Backend.Services.SellerServiecs.SellerServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
@Service
public class SellerServiceImpl implements SellerServices {

    private final SellerRepository sellerRepository;

    private final ShopRepository shopRepository;

    @Autowired
    public SellerServiceImpl(SellerRepository sellerRepository, ShopRepository shopRepository) {
        this.sellerRepository = sellerRepository;
        this.shopRepository = shopRepository;
    }

    @Override
    public Seller createSeller(Seller seller) {

        return sellerRepository.save(seller);
    }

    @Override
    public Seller fetchSellerById(Long sellerId) {

        AtomicReference<Seller> retrievedSeller = new AtomicReference<>();

        sellerRepository.findById(sellerId).ifPresent(retrievedSeller::set);

        return retrievedSeller.get();
    }

    @Override
    public Seller fetchSellerByName(String sellerName) {

        return sellerRepository.findDistinctBySellerFullName(sellerName);
    }

    @Override
    public List<Seller> allSellers() {

        return sellerRepository.findAll();
    }

    @Override
    public List<Seller> allSellersInShop(Long shopId) {

        return shopRepository
                .findById(shopId)
                .map(shop -> new ArrayList<>(shop.getSellers()))
                .orElse(null);
    }

    @Override
    public Seller deleteSeller(Long sellerId) {

        AtomicReference<Seller> sellerToDelete = new AtomicReference<>();

        sellerRepository.findById(sellerId).ifPresent(seller -> {

            sellerToDelete.set(seller);
            sellerRepository.delete(seller);
        });

        return sellerToDelete.get();
    }

    @Override
    public Seller updateSeller(Long sellerId, Seller sellerUpdates) {

        AtomicReference<Seller> updatedSeller = new AtomicReference<>();

        sellerRepository.findById(sellerId).ifPresent(seller -> {

            seller.setSellerAddress(sellerUpdates.getSellerAddress() != null ?
                    sellerUpdates.getSellerAddress() : seller.getSellerAddress());
            seller.setSellerFullName(sellerUpdates.getSellerFullName() != null ?
                    sellerUpdates.getSellerFullName() : seller.getSellerFullName());
            seller.setSellerPhoneNumber(sellerUpdates.getSellerPhoneNumber() != null ?
                    sellerUpdates.getSellerPhoneNumber() : seller.getSellerPhoneNumber());
            updatedSeller.set(sellerRepository.save(seller));
        });

        return updatedSeller.get();
    }
}
