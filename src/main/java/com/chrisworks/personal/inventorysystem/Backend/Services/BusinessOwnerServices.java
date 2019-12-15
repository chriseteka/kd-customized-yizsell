package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import org.springframework.web.context.request.WebRequest;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
public interface BusinessOwnerServices{

    //Services peculiar to business owner
    BusinessOwner createAccount(BusinessOwner businessOwner, WebRequest request);

    BusinessOwner updateAccount(Long businessOwnerId, BusinessOwner updates);

    Seller activateSeller(Long sellerId);

    Seller deactivateSeller(Long sellerId);

    Seller updateSeller(Long sellerId, Seller sellerUpdates);
}
