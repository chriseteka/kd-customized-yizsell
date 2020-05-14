package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Promo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 5/14/2020
 * @email chriseteka@gmail.com
 */
public interface PromoRepository extends JpaRepository<Promo, Long> {

    List<Promo> findAllByCreatedBy(String createdBy);
    List<Promo> findDistinctByPromoNameAndCreatedBy(String promoName, String createdBy);
}
