package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Invoice findDistinctByInvoiceNumber(String invoiceId);

    List<Invoice> findAllByDebtGreaterThan(BigDecimal debtLimit);
}
