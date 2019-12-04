package com.chrisworks.personal.inventorysystem.Backend.Services.BusinessOwnerServices;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
public interface BusinessOwnerServices{

    //Services peculiar to business owner
    BusinessOwner createAccount(BusinessOwner businessOwner);

    BusinessOwner updateAccount(Long businessOwnerId, BusinessOwner updates);

    BusinessOwner fetchBusinessOwner(Long id);

    Boolean approveIncome(Long incomeId);

    Boolean approveExpense(Long expenseId);

    Boolean approveReturn(Long returnedStockId);
}
