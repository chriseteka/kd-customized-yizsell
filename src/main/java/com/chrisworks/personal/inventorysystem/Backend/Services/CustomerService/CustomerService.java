package com.chrisworks.personal.inventorysystem.Backend.Services.CustomerService;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/28/2019
 * @email chriseteka@gmail.com
 */
public interface CustomerService {

    Customer fetchCustomerByPhoneNumber(String customerPhoneNumber);

    List<Customer> fetchAllCustomers();

    List<Customer> fetchAllCustomersWithDebt(BigDecimal debtLimit);

    List<Customer> fetchAllCustomersWithReturnedPurchases();

    Customer updateCustomerDetails(Long customerId, Customer customerUpdates);
}
