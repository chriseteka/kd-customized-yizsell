package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Loyalty;

/**
 * @author Chris_Eteka
 * @since 1/16/2020
 * @email chriseteka@gmail.com
 */
public interface LoyaltyServices extends CRUDServices<Loyalty> {

    Loyalty addCustomerToLoyaltyPlan(Long loyaltyId, Long customerId);

    Loyalty findLoyaltyPlanByCustomerId(Long customerId);

    Loyalty removeCustomerFromPlan(Long loyaltyId, Long customerId);
}
