package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDataValidationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDuplicateEntryException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.BusinessOwnerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ShopRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
@Service
public class SellerServiceImpl implements SellerServices {

    private final SellerRepository sellerRepository;

    private final ShopRepository shopRepository;

    private final BusinessOwnerRepository businessOwnerRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public SellerServiceImpl(SellerRepository sellerRepository, ShopRepository shopRepository,
                             BCryptPasswordEncoder passwordEncoder, BusinessOwnerRepository businessOwnerRepository) {
        this.sellerRepository = sellerRepository;
        this.shopRepository = shopRepository;
        this.passwordEncoder = passwordEncoder;
        this.businessOwnerRepository = businessOwnerRepository;
    }

    @Override
    public Seller createSeller(Seller seller) {

        if (sellerRepository.findDistinctBySellerEmail(seller.getSellerEmail()) != null) throw new
                InventoryAPIDuplicateEntryException("Email already exist", "A seller account already exist with the email address: " +
                seller.getSellerEmail(), null);

        if (businessOwnerRepository.findDistinctByBusinessOwnerEmail(seller.getSellerEmail()) != null) throw new
                InventoryAPIDuplicateEntryException("Email already exist", "A business account already exist with the email address: " +
                seller.getSellerEmail(), null);

        seller.setSellerPassword
                (passwordEncoder.encode(seller.getSellerPassword()));

        return sellerRepository.save(seller);
    }

    @Override
    public Seller fetchSellerById(Long sellerId) {

        if (null == sellerId || sellerId < 0 || !sellerId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("seller id error", "seller id is empty or not a valid number", null);

        return sellerRepository.findById(sellerId).orElse(null);
    }

    @Override
    public Seller fetchSellerByName(String sellerName) {

        return sellerRepository.findDistinctBySellerFullNameOrSellerEmail(sellerName, sellerName);
    }

    @Override
    public List<Seller> allSellers(List<Long> warehouseIds) {

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
