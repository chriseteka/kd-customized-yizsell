package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.SalesDiscount;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockSold;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface SalesDiscountServices {

    void generateDiscountOnStockSold(StockSold stockSold, BigDecimal basePrice, String invoiceNumber, String customer);

    void generateDiscountOnInvoice(String invoiceNumber, BigDecimal discount);

    void generateDiscountOnLoyalCustomers(Customer customer, BigDecimal discount);

    List<SalesDiscount> fetchAllSalesDiscount();

    List<SalesDiscount> fetchAllSalesDiscountByType(String discountType);

    List<SalesDiscount> fetchAllSalesDiscountByDate(Date date);

    List<SalesDiscount> fetchAllSalesDiscountByInvoice(String invoiceNumber);

    List<SalesDiscount> fetchAllBySellerEmail(String createdBy);
}
