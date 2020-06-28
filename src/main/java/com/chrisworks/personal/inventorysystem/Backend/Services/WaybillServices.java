package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/26/2019
 * @email chriseteka@gmail.com
 */
public interface WaybillServices {

    WaybillInvoice requestStockFromWarehouse(Long warehouseId, List<WaybillOrder> stocks);

    WaybillInvoice confirmAndShipWaybill(Long warehouseId, String waybillInvoiceNumber, BigDecimal expense);

    WaybillInvoice confirmShipment(String waybillInvoiceNumber);

    WaybillInvoice findByInvoiceNumber(String waybillInvoiceNumber);

    WaybillInvoice findById(Long waybillInvoiceId);

    List<WaybillInvoice> findAllInShop(Long shopId);

    List<WaybillInvoice> findAllInWarehouse(Long warehouseId);

    List<WaybillInvoice> findAllByCreator(String createdBy);

    List<WaybillInvoice> findAllByIssuer(String issuedBy);

    List<WaybillInvoice> findAllByDateRequested(Date dateRequested);

    List<WaybillInvoice> findAllByDateShipped(Date dateShipped);

    List<WaybillInvoice> findAllDeliveredSuccessfully();

    List<WaybillInvoice> findAllPendingRequests();

    List<WaybillInvoice> findAllCurrentlyShipped();

    List<WaybilledStocks> allStocksInWaybillInvoiceId(Long waybillInvoiceId);

    List<WaybilledStocks> allStocksInWaybillInvoiceNumber(String waybillInvoiceNumber);

    WaybillInvoice deleteWaybillInvoiceById(Long waybillInvoiceId);

    List<WaybillInvoice> deleteWaybillInvoice(Long... waybillInvoiceId);
}
