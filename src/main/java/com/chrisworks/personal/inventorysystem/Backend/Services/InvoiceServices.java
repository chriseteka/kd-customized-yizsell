package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Invoice;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
public interface InvoiceServices extends CRUDServices<Invoice> {

    Invoice clearDebt(String invoiceNumber, BigDecimal amount);

    Invoice fetchInvoiceByInvoiceNumber(String invoiceNumber);

    List<Invoice> fetchAllInvoicesCreatedBy(String createdBy);

    List<Invoice> fetchAllInvoiceCreatedOn(Date createdOn);

    List<Invoice> fetchAllInvoiceCreatedBetween(Date from, Date to);

    List<Invoice> fetchAllInvoiceInShop(Long shopId);

    List<Invoice> fetchAllInvoiceWithDebt();

    List<Invoice> fetchAllInvoiceByPaymentMode(int paymentModeValue);

    List<Invoice> fetchAllInvoicesBySeller(Long sellerId);

    List<Invoice> fetchInvoicesByCustomer(Long customerId);

    List<LedgerReport> fetchInvoicesGroupByCustomers();
}

@Data
@AllArgsConstructor
class LedgerReport {
    private Customer customer;
    private List<Invoice> invoices;
}
