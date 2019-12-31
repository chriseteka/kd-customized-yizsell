package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
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
import java.util.stream.Collectors;


/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
@Service
public class SellerServicesImpl implements SellerServices {

    private final SellerRepository sellerRepository;

    private final BusinessOwnerRepository businessOwnerRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    private final GenericService genericService;

    @Autowired
    public SellerServicesImpl(SellerRepository sellerRepository, BCryptPasswordEncoder passwordEncoder,
                              BusinessOwnerRepository businessOwnerRepository, GenericService genericService) {
        this.sellerRepository = sellerRepository;
        this.passwordEncoder = passwordEncoder;
        this.businessOwnerRepository = businessOwnerRepository;
        this.genericService = genericService;
    }

    @Override
    public Seller createSeller(Seller seller) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

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

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

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

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        Seller sellerFound = sellerRepository.findDistinctBySellerFullNameOrSellerEmail(sellerName, sellerName);

        if (sellerFound == null) throw new InventoryAPIResourceNotFoundException("Seller not found", "No seller" +
                " with the name/email: " + sellerName + " was found", null);

        if (!sellerFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
            throw new InventoryAPIOperationException("Not your seller", "Seller with name/email: " + sellerName +
                    " was not created by you", null);

        return sellerFound;
    }

    @Override
    public List<Seller> fetchAllWarehouseAttendantByWarehouseId(Long warehouseId) {

        return genericService.sellersByAuthUserId()
                .stream()
                .filter(warehouseAttendant -> warehouseAttendant.getWarehouse() != null
                        && warehouseAttendant.getWarehouse().getWarehouseId().equals(warehouseId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Seller> fetchAllShopSellersByShopId(Long shopId) {

        return genericService.sellersByAuthUserId()
                .stream()
                .filter(seller -> seller.getShop() != null
                        && seller.getShop().getShopId().equals(shopId))
                .collect(Collectors.toList());
    }

    @Override
    public Seller deleteSeller(Long sellerId) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return sellerRepository.findById(sellerId).map(seller -> {

            if (!seller.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Not your seller", "Seller with id: " + sellerId +
                        " was not created by you", null);

            sellerRepository.delete(seller);
            return seller;
        }).orElse(null);
    }

    @Override
    public List<Seller> deleteSellerList(List<Long> sellerIds) {

        List<Seller> deletedSellers = new ArrayList<>();

        sellerIds.forEach(sellerId -> deletedSellers.add(deleteSeller(sellerId)));

        return deletedSellers;
    }

    @Override
    public List<Seller> fetchShopSellersByLoggedInUser() {

        return genericService.shopByAuthUserId()
                .stream()
                .map(sellerRepository::findAllByShop)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());
    }

    @Override
    public List<Seller> fetchWarehouseAttendantsByLoggedInUser() {

        return genericService.warehouseByAuthUserId()
                .stream()
                .map(sellerRepository::findAllByWarehouse)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());
    }

    @Override
    public List<Seller> fetchSellers() {

        return genericService.sellersByAuthUserId();
    }

    @Override
    public Seller updateSeller(Long sellerId, Seller sellerUpdates) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return sellerRepository.findById(sellerId).map(seller -> {

            if (!seller.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Not your seller", "Seller with id: " + sellerId +
                        " was not created by you", null);

            seller.setSellerAddress(sellerUpdates.getSellerAddress() != null ?
                    sellerUpdates.getSellerAddress() : seller.getSellerAddress());
            seller.setSellerFullName(sellerUpdates.getSellerFullName() != null ?
                    sellerUpdates.getSellerFullName() : seller.getSellerFullName());
            seller.setSellerPhoneNumber(sellerUpdates.getSellerPhoneNumber() != null ?
                    sellerUpdates.getSellerPhoneNumber() : seller.getSellerPhoneNumber());

            return sellerRepository.save(seller);
        }).orElse(null);
    }
}
