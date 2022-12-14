package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Invoice;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Invoice findDistinctByInvoiceNumber(String invoiceId);

    Invoice findDistinctByInvoiceNumberAndDebtGreaterThan(String invoiceId, BigDecimal debtLimit);

    List<Invoice> findAllBySellerAndDebtGreaterThan(Seller seller, BigDecimal debtLimit);

    List<Invoice> findAllByCreatedBy(String createdBy);

    List<Invoice> findAllBySeller(Seller seller);

    List<Invoice> findAllByCustomerId(Customer customer);

    List<Invoice> findAllByCustomerIdAndDebtGreaterThan(Customer customer, BigDecimal debt);

    List<Invoice> findAllByShop(Shop shop);
}
