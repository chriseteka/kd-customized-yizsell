package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockSold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
public interface StockSoldRepository extends JpaRepository<StockSold, Long> {

    StockSold findDistinctByStockSoldInvoiceIdAndStockName(String invoiceId, String stockName);

    List<StockSold> findAllByCreatedBy(String createdBy);

    List<StockSold> findAllByStockSoldInvoiceId(String invoiceId);
}
