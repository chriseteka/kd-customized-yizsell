package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Supplier;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
public interface SupplierServices extends CRUDServices<Supplier> {

    Supplier fetchSupplierByPhoneNumber(String phoneNumber);

    Supplier fetchSupplierByName(String supplierName);

    List<Supplier> fetchSupplierByCreator(String createdBy);
}
