package com.chrisworks.personal.inventorysystem.Backend.Services.CRUDServices;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ReturnedStock;

import java.util.Date;
import java.util.List;

public interface ReturnedStockServices {

    ReturnedStock fetchStockReturnedByInvoiceNumberAndStockName(String invoiceNumber, String stockName);

    List<ReturnedStock> fetchAllStockReturnedTo(String userFullName);

    List<ReturnedStock> fetchAllStockReturnedWithInvoice(String invoiceNumber);

    List<ReturnedStock> fetchAllStockReturnedByCustomer(Long customerId);

    List<ReturnedStock> fetchAllApprovedReturns();

    List<ReturnedStock> fetchAllUnapprovedReturns();

    List<ReturnedStock> fetchAllReturnsWithin(Date startDate, Date toDate);

}
