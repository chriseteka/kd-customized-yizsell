package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Supplier findBySupplierPhoneNumber(String phoneNumber);

    List<Supplier> findAllByCreatedBy(String createdBy);
}
