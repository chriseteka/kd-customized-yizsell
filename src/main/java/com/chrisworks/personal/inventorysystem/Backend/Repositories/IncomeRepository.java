package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.INCOME_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Income;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findAllByCreatedBy(String createdBy);

    List<Income> findAllByShop(Shop shop);

    List<Income> findAllByIncomeReferenceContains(String invoiceNumber);
}
