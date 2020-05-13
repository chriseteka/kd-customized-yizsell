package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.APPLICATION_EVENTS;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.WaybillInvoice;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.WaybillOrder;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Services.WaybillServices;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.chrisworks.personal.inventorysystem.Backend.Utility.Events.SellerTriggeredEvent;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.controllers.WebsocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Entities.ListWrapper.prepareResponse;
import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.formatMoney;

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
    private final WebsocketController websocketController;
    private final ApplicationEventPublisher eventPublisher;
    private String description = null;

    @Autowired
    public WaybillController(WaybillServices waybillServices, WebsocketController websocketController,
                             ApplicationEventPublisher eventPublisher) {
        this.waybillServices = waybillServices;
        this.websocketController = websocketController;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping(path = "/request/fromWarehouse", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> requestWaybillFromWarehouse(@RequestParam Long warehouseId,
                                                         @RequestBody @Valid List<WaybillOrder> orders){

        WaybillInvoice waybillInvoice = waybillServices.requestStockFromWarehouse(warehouseId, orders);

        if (null == waybillInvoice) throw new InventoryAPIOperationException("Waybill request failed",
                "Ware bill request failed, review your inputs and try again", null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            description = "A ware bill request has been placed, details: \nfrom: " + waybillInvoice.getShop().getShopName()
                    + "\nby: " + waybillInvoice.getSellerRequesting().getSellerFullName()
                    + "\nto warehouse: " + waybillInvoice.getWarehouse().getWarehouseName();
            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    description, APPLICATION_EVENTS.WARE_BILL_REQUEST_EVENT));
            websocketController.sendNoticeToWarehouseAttendants(waybillInvoice.getWarehouse(), description);
            websocketController.sendNoticeToUser(description, waybillInvoice.getSellerRequesting().getCreatedBy());
        }

        return ResponseEntity.ok(waybillInvoice);
    }

    @PutMapping(path = "/beginShipment")
    public ResponseEntity<?> confirmOrderAndBeginShipment(@RequestParam Long warehouseId,
                                                          @RequestParam String wareBillNumber,
                                                          @RequestParam Long expense){

        WaybillInvoice waybillInvoice = waybillServices.confirmAndShipWaybill(warehouseId, wareBillNumber, BigDecimal.valueOf(expense));

        if (null == waybillInvoice) throw new InventoryAPIOperationException("Confirmation failed",
                "Ware bill confirmation and shipment was not successful, review your inputs and try again", null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            description = "A ware bill request has been confirmed and shipped, details: "
                    + "\nfrom warehouse: " + waybillInvoice.getWarehouse().getWarehouseName()
                    + "\nby: " + waybillInvoice.getSellerIssuing().getSellerFullName()
                    + "\nto shop: " + waybillInvoice.getShop().getShopName()
                    +"\nStock worth: " + formatMoney(waybillInvoice.getWaybillInvoiceTotalAmount());
            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    description, APPLICATION_EVENTS.WARE_BILL_ISSUED_AND_SHIPPED_EVENT));
            websocketController.sendNoticeToUser(description, waybillInvoice.getCreatedBy(),
                    waybillInvoice.getSellerIssuing().getCreatedBy());
        }

        return ResponseEntity.ok(waybillInvoice);
    }

    @PutMapping(path = "/confirmShipment")
    public ResponseEntity<?> confirmShipment(@RequestParam String wareBillNumber){

        WaybillInvoice waybillInvoice = waybillServices.confirmShipment(wareBillNumber);

        if (null == waybillInvoice) throw new InventoryAPIOperationException("Confirmation failed",
                "Could not confirm shipments, review your inputs and try again", null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            description = "A ware bill request has been received and recorded, details:"
                    + "\nto shop: " + waybillInvoice.getShop().getShopName()
                    + "\nby: " + waybillInvoice.getSellerRequesting().getSellerFullName()
                    + "\nat shop: " + waybillInvoice.getShop().getShopName()
                    + "\nStock Worth: " + formatMoney(waybillInvoice.getWaybillInvoiceTotalAmount());
            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    description, APPLICATION_EVENTS.WARE_BILL_RECEIVED_EVENT));
            websocketController.sendNoticeToUser(description, waybillInvoice.getIssuedBy(),
                    waybillInvoice.getSellerRequesting().getCreatedBy());
        }

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
    public ResponseEntity<?> findByWaybillInvoiceInShop(@RequestParam Long shopId, @RequestParam int page,
                                                        @RequestParam int size){

        List<WaybillInvoice> waybillInvoiceList = waybillServices.findAllInShop(shopId)
                .stream()
                .sorted(Comparator.comparing(WaybillInvoice::getCreatedDate).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(prepareResponse(waybillInvoiceList, page, size));
    }

    @GetMapping(path = "/byWarehouse")
    public ResponseEntity<?> findByWaybillInvoiceInWarehouse(@RequestParam Long warehouseId, @RequestParam int page,
                                                             @RequestParam int size){

        List<WaybillInvoice> waybillInvoiceList = waybillServices.findAllInWarehouse(warehouseId)
                .stream()
                .sorted(Comparator.comparing(WaybillInvoice::getCreatedDate).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(prepareResponse(waybillInvoiceList, page, size));
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
    public ResponseEntity<?> findAllWaybillInvoicePendingConfirmAndChip(@RequestParam int page,
                                                                        @RequestParam int size){

        List<WaybillInvoice> allPendingRequests = waybillServices.findAllPendingRequests()
                .stream()
                .sorted(Comparator.comparing(WaybillInvoice::getCreatedDate).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(prepareResponse(allPendingRequests, page, size));
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
