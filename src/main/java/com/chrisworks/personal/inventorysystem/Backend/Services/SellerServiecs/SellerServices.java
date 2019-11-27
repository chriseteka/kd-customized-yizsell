package com.chrisworks.personal.inventorysystem.Backend.Services.SellerServiecs;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
public interface SellerServices {

    //Services peculiar to sellers only
    Seller updateAccount(Long userId, Seller seller);
}
