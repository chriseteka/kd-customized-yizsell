package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model.ResponseObject;
import com.chrisworks.personal.inventorysystem.Backend.Entities.DesktopPushObject;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.*;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
@Service
public class ShopServicesImpl implements ShopServices {

    private final ShopRepository shopRepository;

    private final BusinessOwnerRepository businessOwnerRepository;

    @Autowired
    public ShopServicesImpl(ShopRepository shopRepository,
                            BusinessOwnerRepository businessOwnerRepository) {
        this.shopRepository = shopRepository;
        this.businessOwnerRepository = businessOwnerRepository;
    }

    @Override
    public Shop createShop(Long businessOwnerId, Shop shop) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        Optional<BusinessOwner> businessOwner = businessOwnerRepository.findById(businessOwnerId);

        if (!businessOwner.isPresent()) throw new InventoryAPIOperationException
                ("Unknown user", "Could not detect the user trying to create a new shop", null);

        verifyShopCreationLimitViolation(businessOwner.get());

        if (shopRepository.findDistinctByShopNameAndCreatedBy(shop.getShopName(),
                AuthenticatedUserDetails.getUserFullName()) != null) throw new InventoryAPIOperationException
                ("Shop name already exist", "A shop already exist with the name: " + shop.getShopName(), null);

        shop.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
        shop.setBusinessOwner(businessOwner.get());
        return shopRepository.save(shop);
    }

    @Override
    public Shop findShopById(Long shopId) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return shopRepository.findById(shopId)
                .map(shop -> {

                    if (!shop.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                        throw new InventoryAPIOperationException
                                ("shop is not yours", "This shop was not created by you", null);

                    return shop;
                }).orElse(null);
    }

    @Override
    public Shop updateShop(Long shopId, Shop shopUpdates) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return shopRepository.findById(shopId).map(shop -> {

            if (!shop.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException
                        ("shop is not yours", "This shop was not created by you", null);

            shop.setUpdateDate(new Date());
            shop.setShopName(shopUpdates.getShopName());
            shop.setShopAddress(shopUpdates.getShopAddress());
            return shopRepository.save(shop);
        }).orElse(null);
    }

    @Override
    public List<Shop> fetchAllShops() {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return shopRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());
    }

    @Override
    public Shop deleteShop(Long shopId) {

        return shopRepository.findById(shopId).map(shop -> {

            if (!shop.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())) throw new
                    InventoryAPIOperationException("Not your seller", "Seller not created by you", null);

            shopRepository.delete(shop);
            return shop;
        }).orElseThrow(() -> new InventoryAPIResourceNotFoundException("Shop not found",
                "Shop with id: " + shopId + " was not found", null));
    }

    @Override
    public List<Shop> deleteShops(Long... shopIds) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        List<Long> shopIdsToDelete = Arrays.asList(shopIds);
        if (shopIdsToDelete.size() == 1)
            return Collections.singletonList(deleteShop(shopIdsToDelete.get(0)));

        List<Shop> shopsToDelete = fetchAllShops().stream()
                .filter(shop -> shopIdsToDelete.contains(shop.getShopId()))
                .collect(Collectors.toList());

        if (!shopsToDelete.isEmpty()) shopRepository.deleteAll(shopsToDelete);

        return shopsToDelete;
    }

    @Override
    public ResponseObject receiveDesktopPush(DesktopPushObject desktopPushObject) {

        System.out.println("Received**** " + desktopPushObject.toString());
        return new ResponseObject(true, "Push completed.");
    }

    private void verifyShopCreationLimitViolation(BusinessOwner businessOwner) {

        if (shopRepository.findAllByCreatedBy(businessOwner.getBusinessOwnerEmail()).size()
                >= businessOwner.getPlan().getNumberOfShops())
            throw new InventoryAPIOperationException("Operation not allowed",
                    "You have reached the maximum number of shops you can create in your plan/subscription", null);
    }
}
