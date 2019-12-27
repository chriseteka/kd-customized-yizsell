package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Customer findDistinctByCustomerPhoneNumber(String phoneNumber);

    Customer findDistinctByCustomerEmail(String customerEmail);

    List<Customer> findAllByCreatedBy(String createdBy);
}
