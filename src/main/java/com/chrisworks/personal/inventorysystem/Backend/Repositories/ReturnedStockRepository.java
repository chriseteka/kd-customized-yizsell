package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ReturnedStock;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
@Repository
public interface ReturnedStockRepository extends JpaRepository<ReturnedStock, Long> {

    ReturnedStock findAllByInvoiceIdAndStockName(String invoiceId, String stockName);

    List<ReturnedStock> findAllByCreatedBy(String userFullName);

    List<ReturnedStock> findAllByShop(Shop shop);

    List<ReturnedStock> findAllByCustomerId(Customer customer);
}
