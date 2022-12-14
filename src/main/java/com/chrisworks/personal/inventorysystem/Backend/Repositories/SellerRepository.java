package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
public interface SellerRepository extends JpaRepository<Seller, Long> {

    Seller findDistinctBySellerFullNameOrSellerEmail(String sellerName, String sellerEmail);

    Seller findDistinctBySellerEmail(String email);

    List<Seller> findAllByShop(Shop shop);

    List<Seller> findAllByWarehouse(Warehouse warehouse);

    List<Seller> findAllByCreatedBy(String createdBy);
}
