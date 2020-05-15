package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Promo;

/**
 * @author Chris_Eteka
 * @since 5/14/2020
 * @email chriseteka@gmail.com
 */
public interface PromoServices extends CRUDServices<Promo> {

    Promo addStockToPromo(Long promoId, Long stockId);

    Promo removeStockFromPromo(Long promoId, Long stockId);
}
