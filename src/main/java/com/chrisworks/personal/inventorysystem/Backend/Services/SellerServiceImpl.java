package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDuplicateEntryException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.BusinessOwnerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    private final BusinessOwnerRepository businessOwnerRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public SellerServiceImpl(SellerRepository sellerRepository, BCryptPasswordEncoder passwordEncoder,
                             BusinessOwnerRepository businessOwnerRepository) {
        this.sellerRepository = sellerRepository;
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

        seller.setIsActive(true);
        seller.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
        seller.setSellerPassword
                (passwordEncoder.encode(seller.getSellerPassword()));

        return sellerRepository.save(seller);
    }

    @Override
    public Seller fetchSellerById(Long sellerId) {

        if (null == sellerId || sellerId < 0 || !sellerId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("seller id error", "seller id is empty or not a valid number", null);

        return sellerRepository.findById(sellerId)
                .map(seller -> {

                    if (!seller.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                        throw new InventoryAPIOperationException("Not your seller", "Seller with id: " + sellerId +
                                " was not created by you", null);
                    return seller;
                }).orElse(null);
    }

    @Override
    public Seller fetchSellerByNameOrEmail(String sellerName) {

        Seller sellerFound = sellerRepository.findDistinctBySellerFullNameOrSellerEmail(sellerName, sellerName);

        if (sellerFound == null) throw new InventoryAPIResourceNotFoundException("Seller not found", "No seller" +
                " with the name/email: " + sellerName + " was found", null);

        if (!sellerFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
            throw new InventoryAPIOperationException("Not your seller", "Seller with name/email: " + sellerName +
                    " was not created by you", null);

        return sellerFound;
    }

    @Override
    public List<Seller> allSellersByWarehouseId(Long warehouseId) {

        return sellerRepository.findAll()
                .stream()
                .filter(seller -> seller.getWarehouse() != null
                        && seller.getWarehouse().getWarehouseId().equals(warehouseId)
                        && seller.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Seller> allSellersByShopId(Long shopId) {

        return sellerRepository.findAll()
                .stream()
                .filter(seller -> seller.getShop() != null
                        && seller.getShop().getShopId().equals(shopId)
                        && seller.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                .collect(Collectors.toList());
    }

    @Override
    public Seller deleteSeller(Long sellerId) {

        return sellerRepository.findById(sellerId).map(seller -> {

            if (!seller.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Not your seller", "Seller with id: " + sellerId +
                        " was not created by you", null);

            sellerRepository.delete(seller);
            return seller;
        }).orElse(null);
    }

    @Override
    public List<Seller> deleteSellerList(List<Seller> sellerList) {

        List<Seller> deletedSellers = new ArrayList<>();

        sellerList.forEach(seller -> deletedSellers.add(deleteSeller(seller.getSellerId())));

        return deletedSellers;
    }

    @Override
    public List<Seller> fetchSellerByShop(Shop shop) {

        return sellerRepository.findAllByShop(shop)
                .stream()
                .filter(seller -> seller.getCreatedBy()
                        .equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Seller> fetchSellerByWarehouse(Warehouse warehouse) {

        return sellerRepository.findAllByWarehouse(warehouse)
                .stream()
                .filter(seller -> seller.getCreatedBy()
                        .equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Seller> fetchSellers() {

        return sellerRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());
    }

    @Override
    public Seller updateSeller(Long sellerId, Seller sellerUpdates) {

        AtomicReference<Seller> updatedSeller = new AtomicReference<>(null);

        sellerRepository.findById(sellerId).ifPresent(seller -> {

            if (!seller.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Not your seller", "Seller with id: " + sellerId +
                        " was not created by you", null);

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
