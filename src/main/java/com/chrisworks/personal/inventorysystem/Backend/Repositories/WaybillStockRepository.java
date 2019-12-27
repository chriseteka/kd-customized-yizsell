package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.WaybilledStocks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/24/2019
 * @email chriseteka@gmail.com
 */
public interface WaybillStockRepository extends JpaRepository<WaybilledStocks, Long> {

    List<WaybilledStocks> findAllByStockWaybillInvoiceId(String waybillInvoiceId);
}
