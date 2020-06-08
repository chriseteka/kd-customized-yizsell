package com.chrisworks.personal.inventorysystem.Backend.Services.PreEmptiveServices;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer;

/**
 * @author Chris_Eteka
 * @since 6/8/2020
 * @email chriseteka@gmail.com
 */
public interface CustomerPreEmptives {

    void detachCustomersFromObjects(Customer customer);
}
