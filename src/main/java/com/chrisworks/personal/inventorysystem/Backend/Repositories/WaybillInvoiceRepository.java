package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.WaybillInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/24/2019
 * @email chriseteka@gmail.com
 */
public interface WaybillInvoiceRepository extends JpaRepository<WaybillInvoice, Long> {

    WaybillInvoice findDistinctByWaybillInvoiceNumber(String waybillInvoiceNumber);

    List<WaybillInvoice> findAllByShop(Shop shop);

    List<WaybillInvoice> findAllByWarehouse(Warehouse warehouse);

    List<WaybillInvoice> findAllByCreatedBy(String createdBy);

    List<WaybillInvoice> findAllByIssuedBy(String issuedBy);
}
