package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ReturnedStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;


/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
public interface ReturnedStockRepository extends JpaRepository<ReturnedStock, Long> {

    ReturnedStock findAllByInvoiceIdAndStockName(String invoiceId, String stockName);

    List<ReturnedStock> findAllByCreatedBy(String userFullName);

    List<ReturnedStock> findAllByInvoiceId(String invoiceId);

    List<ReturnedStock> findAllByCustomerId(Customer customer);

    List<ReturnedStock> findAllByApprovedTrue();

    List<ReturnedStock> findAllByApprovedFalse();

    List<ReturnedStock> findAllByCreatedDateIsBetween(Date fromDate, Date toDate);
}
