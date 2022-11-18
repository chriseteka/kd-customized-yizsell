package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.APPLICATION_EVENTS;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ListWrapper;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Invoice;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Services.InvoiceServices;
import com.chrisworks.personal.inventorysystem.Backend.Services.LedgerReport;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.chrisworks.personal.inventorysystem.Backend.Utility.Events.SellerTriggeredEvent;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.controllers.WebsocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Entities.ListWrapper.prepareResponse;
import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.formatMoney;

@RestController
@RequestMapping("/invoice")
public class InvoiceController {

    private final InvoiceServices invoiceServices;
    private final WebsocketController websocketController;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public InvoiceController(InvoiceServices invoiceServices, WebsocketController websocketController,
                             ApplicationEventPublisher eventPublisher) {
        this.invoiceServices = invoiceServices;
        this.websocketController = websocketController;
        this.eventPublisher = eventPublisher;
    }

    @GetMapping(path = "/byId")
    public ResponseEntity<?> getInvoiceById(@RequestParam Long invoiceId){

        Invoice invoice = invoiceServices.getSingleEntity(invoiceId);

        if (null == invoice) throw new InventoryAPIResourceNotFoundException("Invoice not found",
                "Invoice with id: " + invoiceId + " was not found", null);

        return ResponseEntity.ok(invoice);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<?> getAllInvoices(@RequestParam int page, @RequestParam int size,
                                            @RequestParam(required = false, defaultValue = "") String search){

        return ResponseEntity.ok(paginatedInvoiceList(invoiceServices.getEntityList(), search, page, size));
    }

    @GetMapping(path = "/all/groupByCustomer")
    public ResponseEntity<?> getAllInvoicesGroupedByCustomers(@RequestParam int page, @RequestParam int size,
                                                              @RequestParam(required = false, defaultValue = "") String search){

        return ResponseEntity.ok(paginatedLedgerList(invoiceServices.fetchInvoicesGroupByCustomers(), search, page, size));
    }

    @GetMapping(path = "/all/withDebt/groupByCustomer")
    public ResponseEntity<?> getAllInvoicesWithDebtGroupedByCustomers(@RequestParam int page, @RequestParam int size,
                                                                      @RequestParam(required = false, defaultValue = "") String search){

        return ResponseEntity.ok(paginatedLedgerList(invoiceServices.fetchInvoicesWithDebtGroupByCustomers(), search, page, size));
    }

    @GetMapping(path = "/all/withDebt/groupByCustomer/byShop")
    public ResponseEntity<?> getAllInvoicesWithDebtGroupedByCustomers(@RequestParam Long shopId, @RequestParam int page, @RequestParam int size,
                                                                      @RequestParam(required = false, defaultValue = "") String search){

        return ResponseEntity.ok(paginatedLedgerList(invoiceServices.fetchInvoicesWithDebtGroupByCustomersAndShop(shopId),
                search, page, size));
    }

    @DeleteMapping(path = "/byId")
    public ResponseEntity<?> deleteIncomeById(@RequestParam Long invoiceId){

        Invoice invoice = invoiceServices.deleteEntity(invoiceId);

        if (null == invoice) throw new InventoryAPIResourceNotFoundException("Income not deleted",
                "Income with id: " + invoiceId + " was not deleted.", null);

        return ResponseEntity.ok(invoice);
    }

    @PutMapping(path = "/clearDebt")
    public ResponseEntity<?> clearDebt(@RequestParam String invoiceNumber,
                                       @RequestParam BigDecimal amountPaid){

        Invoice invoice = invoiceServices.clearDebt(invoiceNumber, amountPaid);

        if (null == invoice) throw new InventoryAPIOperationException("Debt clearance error",
                "Debt clearance was not successful, review your inputs and try again", null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            String description = "A debt clearance has been recorded, details: "
                    + "\ninvoice number: " + invoiceNumber
                    + "\namount paid: " + formatMoney(amountPaid)
                    + "\nplease review this action as soon as possible.";
            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    description, APPLICATION_EVENTS.DEBT_CLEARANCE_EVENT));
            websocketController.sendNoticeToUser(description, websocketController.businessOwnerMail());
        }

        return ResponseEntity.ok(invoice);
    }

    @GetMapping(path = "/byNumber")
    public ResponseEntity<?> fetchInvoiceByNumber(@RequestParam String invoiceNumber){

        Invoice invoice = invoiceServices.fetchInvoiceByInvoiceNumber(invoiceNumber);

        if (null == invoice) throw new InventoryAPIResourceNotFoundException("Invoice not found",
                "Invoice with number: " + invoiceNumber + " was not found", null);

        return ResponseEntity.ok(invoice);
    }

    @GetMapping(path = "/byCreator")
    public ResponseEntity<?> fetchInvoicesByCreator(@RequestParam String createdBy){

        return ResponseEntity.ok(invoiceServices.fetchAllInvoicesCreatedBy(createdBy));
    }

    @GetMapping(path = "/byCreatedDate")
    public ResponseEntity<?> fetchInvoicesByCreatedDate(@RequestParam Date createdDate){

        return ResponseEntity.ok(invoiceServices.fetchAllInvoiceCreatedOn(createdDate));
    }

    @GetMapping(path = "/byCreatedBetween")
    public ResponseEntity<?> fetchInvoicesByCreatedDateBetween(@RequestParam Date from,
                                                               @RequestParam Date to){

        return ResponseEntity.ok(invoiceServices.fetchAllInvoiceCreatedBetween(from, to));
    }

    @GetMapping(path = "/byShop")
    public ResponseEntity<?> fetchInvoicesByShop(@RequestParam Long shopId, @RequestParam int page, @RequestParam int size,
                                                 @RequestParam(required = false, defaultValue = "") String search){

        return ResponseEntity.ok(paginatedInvoiceList(invoiceServices.fetchAllInvoiceInShop(shopId), search, page, size));
    }

    @GetMapping(path = "/withDebt")
    public ResponseEntity<?> fetchInvoicesWithDebts(){

        return ResponseEntity.ok(invoiceServices.fetchAllInvoiceWithDebt());
    }

    @GetMapping(path = "/byPaymentMode")
    public ResponseEntity<?> fetchInvoicesByPaymentMode(@RequestParam int paymentMode){

        return ResponseEntity.ok(invoiceServices.fetchAllInvoiceByPaymentMode(paymentMode));
    }

    @GetMapping(path = "/bySeller")
    public ResponseEntity<?> fetchInvoicesBySeller(@RequestParam Long sellerId){

        return ResponseEntity.ok(invoiceServices.fetchAllInvoicesBySeller(sellerId));
    }

    @GetMapping(path = "/byCustomer")
    public ResponseEntity<?> fetchInvoicesByCustomer(@RequestParam Long customerId){

        return ResponseEntity.ok(invoiceServices.fetchInvoicesByCustomer(customerId));
    }

    @PutMapping(path = "/clearDebt/byCustomerId", produces = "application/json")
    public ResponseEntity<?> clearDebtByCustomerId(@RequestParam Long custId,
                                                   @RequestParam BigDecimal amount){
        return ResponseEntity.ok(invoiceServices.clearDebtByCustomerId(custId, amount));
    }

    private ListWrapper paginatedInvoiceList(List<Invoice> invoiceList, String search, int page, int size){

        return prepareResponse(invoiceList.stream()
            .filter(invoice -> {
                if (!StringUtils.hasText(search)) return true;
                return invoice.getCreatedBy().contains(search.toLowerCase())
                    || String.valueOf(invoice.getAmountPaid()).contains(search)
                    || String.valueOf(invoice.getInvoiceNumber()).contains(search)
                    || String.valueOf(invoice.getPaymentMode()).contains(search.toUpperCase());
            }).sorted(Comparator.comparing(Invoice::getCreatedDate).thenComparing(Invoice::getCreatedTime).reversed())
            .collect(Collectors.toList()), page, size);
    }

    private ListWrapper paginatedLedgerList(List<LedgerReport> ledgerReportList, String search, int page, int size){

        return prepareResponse(ledgerReportList.stream()
            .filter(report -> {
                if (!StringUtils.hasText(search)) return true;
                return report.getCustomer().getCustomerFullName().toLowerCase().contains(search.toLowerCase())
                        || String.valueOf(report.getCustomer().getDebt()).contains(search)
                        || String.valueOf(report.getCustomer().getCustomerPhoneNumber()).contains(search)
                        || report.getInvoices().stream().anyMatch(invoice -> invoice.getInvoiceNumber().contains(search));
            }).collect(Collectors.toList()), page, size);
    }
}
