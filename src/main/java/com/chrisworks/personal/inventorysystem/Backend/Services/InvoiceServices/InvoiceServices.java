package com.chrisworks.personal.inventorysystem.Backend.Services.InvoiceServices;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.PAYMENT_MODE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Invoice;
import com.chrisworks.personal.inventorysystem.Backend.Services.CRUDServices.CRUDServices;

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

    List<Invoice> fetchAllInvoicesCreatedBy(String createdBy);

    List<Invoice> fetchAllInvoiceCreatedOn(Date createdOn);

    List<Invoice> fetchAllInvoiceCreatedBetween(Date from, Date to);

    List<Invoice> fetchAllInvoiceInShop(Long shopId);

    List<Invoice> fetchAllInvoiceWithDebt();

    List<Invoice> fetchAllInvoiceByPaymentMode(int paymentModeValue);
}
