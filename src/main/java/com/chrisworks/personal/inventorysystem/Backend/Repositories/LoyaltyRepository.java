package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Loyalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 1/16/2020
 * @email chriseteka@gmail.com
 */
@Repository
public interface LoyaltyRepository extends JpaRepository<Loyalty, Long> {

    List<Loyalty> findAllByCreatedBy(String createdBy);

    Loyalty findDistinctByCustomers(Customer customer);
}
