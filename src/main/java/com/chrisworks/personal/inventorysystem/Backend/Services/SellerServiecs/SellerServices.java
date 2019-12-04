package com.chrisworks.personal.inventorysystem.Backend.Services.SellerServiecs;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
public interface SellerServices {

    //Services peculiar to sellers only
    Seller createSeller(Seller seller);

    Seller fetchSellerById(Long sellerId);

    Seller fetchSellerByName(String sellerName);

    List<Seller> allSellers();

    List<Seller> allSellersInShop(Long shopId);

    Seller updateSeller(Long sellerId, Seller sellerUpdates);

    Seller deleteSeller(Long sellerId);


}
