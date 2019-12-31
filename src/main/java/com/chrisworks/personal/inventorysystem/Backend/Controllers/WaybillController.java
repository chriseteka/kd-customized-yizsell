package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.WaybillInvoice;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.WaybillOrder;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Services.WaybillServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/31/2019
 * @email chriseteka@gmail.com
 */
@RestController
@RequestMapping("/wareBill")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class WaybillController {

    private final WaybillServices waybillServices;

    @Autowired
    public WaybillController(WaybillServices waybillServices) {
        this.waybillServices = waybillServices;
    }

    @PostMapping(path = "/request/fromWarehouse", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> requestWaybillFromWarehouse(@RequestParam Long warehouseId,
                                                         @RequestBody @Valid List<WaybillOrder> orders){

        WaybillInvoice waybillInvoice = waybillServices.requestStockFromWarehouse(warehouseId, orders);

        if (null == waybillInvoice) throw new InventoryAPIOperationException("Waybill request failed",
                "Ware bill request failed, review your inputs and try again", null);

        return ResponseEntity.ok(waybillInvoice);
    }

    @PutMapping(path = "/beginShipment")
    public ResponseEntity<?> confirmOrderAndBeginShipment(@RequestParam Long warehouseId,
                                                          @RequestParam String wareBillNumber,
                                                          @RequestParam BigDecimal expense){

        WaybillInvoice waybillInvoice = waybillServices.confirmAndShipWaybill(warehouseId, wareBillNumber, expense);

        if (null == waybillInvoice) throw new InventoryAPIOperationException("Confirmation failed",
                "Ware bill confirmation and shipment was not successful, review your inputs and try again", null);

        return ResponseEntity.ok(waybillInvoice);
    }

    @PutMapping(path = "/confirmShipment")
    public ResponseEntity<?> confirmShipment(@RequestParam String wareBillNumber){

        WaybillInvoice waybillInvoice = waybillServices.confirmShipment(wareBillNumber);

        if (null == waybillInvoice) throw new InventoryAPIOperationException("Confirmation failed",
                "Could not confirm shipments, review your inputs and try again", null);

        return ResponseEntity.ok(waybillInvoice);
    }

    @GetMapping(path = "/byWareBillNumber")
    public ResponseEntity<?> findByWaybillInvoiceByNumber(@RequestParam String wareBillNumber){

        WaybillInvoice waybillInvoice = waybillServices.findByInvoiceNumber(wareBillNumber);

        if (null == waybillInvoice) throw new InventoryAPIResourceNotFoundException("Ware bill invoice not found",
                "Ware bill invoice with number: " + wareBillNumber +
                        " was not found, review your inputs and try again", null);

        return ResponseEntity.ok(waybillInvoice);
    }

    @GetMapping(path = "/byId")
    public ResponseEntity<?> findByWaybillInvoiceById(@RequestParam Long wareBillId){

        WaybillInvoice waybillInvoice = waybillServices.findById(wareBillId);

        if (null == waybillInvoice) throw new InventoryAPIResourceNotFoundException("Ware bill invoice not found",
                "Ware bill invoice with id: " + wareBillId +
                        " was not found, review your inputs and try again", null);

        return ResponseEntity.ok(waybillInvoice);
    }

    @GetMapping(path = "/byShop")
    public ResponseEntity<?> findByWaybillInvoiceInShop(@RequestParam Long shopId){

        return ResponseEntity.ok(waybillServices.findAllInShop(shopId));
    }

    @GetMapping(path = "/byWarehouse")
    public ResponseEntity<?> findByWaybillInvoiceInWarehouse(@RequestParam Long warehouseId){

        return ResponseEntity.ok(waybillServices.findAllInWarehouse(warehouseId));
    }

    @GetMapping(path = "/byCreator")
    public ResponseEntity<?> findByWaybillInvoiceByCreator(@RequestParam String createdBy){

        return ResponseEntity.ok(waybillServices.findAllByCreator(createdBy));
    }

    @GetMapping(path = "/byIssuer")
    public ResponseEntity<?> findByWaybillInvoiceByIssuer(@RequestParam String issuedBy){

        return ResponseEntity.ok(waybillServices.findAllByIssuer(issuedBy));
    }

    @GetMapping(path = "/byRequestDate")
    public ResponseEntity<?> findByWaybillInvoiceByDateRequested(@RequestParam Date dateRequested){

        return ResponseEntity.ok(waybillServices.findAllByDateRequested(dateRequested));
    }

    @GetMapping(path = "/byShippedDate")
    public ResponseEntity<?> findByWaybillInvoiceByDateShipped(@RequestParam Date dateShipped){

        return ResponseEntity.ok(waybillServices.findAllByDateShipped(dateShipped));
    }

    @GetMapping(path = "/all/delivered")
    public ResponseEntity<?> findAllWaybillInvoiceDeliveredSuccessfully(){

        return ResponseEntity.ok(waybillServices.findAllDeliveredSuccessfully());
    }

    @GetMapping(path = "/all/pending")
    public ResponseEntity<?> findAllWaybillInvoicePendingConfirmAndChip(){

        return ResponseEntity.ok(waybillServices.findAllPendingRequests());
    }

    @GetMapping(path = "/all/currentlyShipped")
    public ResponseEntity<?> findAllWaybillInvoiceCurrentlyShipped(){

        return ResponseEntity.ok(waybillServices.findAllCurrentlyShipped());
    }

    @GetMapping(path = "/all/stocks/byWareBillId")
    public ResponseEntity<?> findAllStocksInWaybillInvoiceByWayBillInvoiceId(@RequestParam Long wareBillId){

        return ResponseEntity.ok(waybillServices.allStocksInWaybillInvoiceId(wareBillId));
    }

    @GetMapping(path = "/all/stocks/byWareBillNumber")
    public ResponseEntity<?> findAllStocksInWaybillInvoiceByWayBillInvoiceNumber(@RequestParam String wareBillNumber){

        return ResponseEntity.ok(waybillServices.allStocksInWaybillInvoiceNumber(wareBillNumber));
    }

    @DeleteMapping(path = "/delete/byWareBillId")
    public ResponseEntity<?> deleteWaybillInvoiceByWaybillInvoiceId(@RequestParam Long wareBillId){

        WaybillInvoice waybillInvoice = waybillServices.deleteWaybillInvoiceById(wareBillId);

        if (null == waybillInvoice) throw new InventoryAPIOperationException("Waybill Invoice not deleted",
                "Ware Bill invoice was not deleted successfully, review your inputs and try again", null);

        return ResponseEntity.ok(waybillInvoice);
    }

    @DeleteMapping(path = "/delete/byWareBillNumber")
    public ResponseEntity<?> deleteWaybillInvoiceByWaybillInvoiceNumber(@RequestParam String wareBillNumber){

        WaybillInvoice waybillInvoice = waybillServices.deleteWaybillInvoiceByNumber(wareBillNumber);

        if (null == waybillInvoice) throw new InventoryAPIOperationException("Waybill Invoice not deleted",
                "Ware Bill invoice was not deleted successfully, review your inputs and try again", null);

        return ResponseEntity.ok(waybillInvoice);
    }
}
