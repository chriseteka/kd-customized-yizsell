package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ReturnedStock;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Services.ReturnedStockServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * @author Chris_Eteka
 * @since 12/30/2019
 * @email chriseteka@gmail.com
 */
@RestController
@RequestMapping("/returnedStock")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReturnedStockController {

    private final ReturnedStockServices returnedStockServices;

    @Autowired
    public ReturnedStockController(ReturnedStockServices returnedStockServices) {
        this.returnedStockServices = returnedStockServices;
    }

    @GetMapping(path = "/byInvoice/andStockName")
    public ResponseEntity<?> fetchReturnsByInvoiceNumberAndStockName(@RequestParam String invoiceNumber,
                                                                     @RequestParam String stockName){

        ReturnedStock returnedStock = returnedStockServices
                .fetchStockReturnedByInvoiceNumberAndStockName(invoiceNumber, stockName);

        if (null == returnedStock) throw new InventoryAPIResourceNotFoundException("Not found",
                "No returned stock was returned with the supplied information", null);

        return ResponseEntity.ok(returnedStock);
    }

    @GetMapping(path = "/toSeller")
    public ResponseEntity<?> fetchAllReturnedStocksTo(@RequestParam String seller){

        return ResponseEntity.ok(returnedStockServices.fetchAllStockReturnedTo(seller));
    }

    @GetMapping(path = "/toShop")
    public ResponseEntity<?> fetchAllReturnedStocksToShop(@RequestParam Long shopId){

        return ResponseEntity.ok(returnedStockServices.fetchAllStockReturnedToShop(shopId));
    }

    @GetMapping(path = "/withInvoice")
    public ResponseEntity<?> fetchAllReturnedStocksWithInvoiceNumber(@RequestParam String invoiceNumber){

        return ResponseEntity.ok(returnedStockServices.fetchAllStockReturnedWithInvoice(invoiceNumber));
    }

    @GetMapping(path = "/byCustomer")
    public ResponseEntity<?> fetchAllReturnedStocksByCustomer(@RequestParam Long customerId){

        return ResponseEntity.ok(returnedStockServices.fetchAllStockReturnedByCustomer(customerId));
    }

    @GetMapping(path = "/all/approved")
    public ResponseEntity<?> fetchAllApprovedReturnedStocks(){

        return ResponseEntity.ok(returnedStockServices.fetchAllApprovedReturns());
    }

    @GetMapping(path = "/all")
    public ResponseEntity<?> fetchAllReturnedStocks(){

        return ResponseEntity.ok(returnedStockServices.fetchAllReturnedStocks());
    }

    @GetMapping(path = "/all/unApproved/byCreator")
    public ResponseEntity<?> fetchAllUnApprovedReturnedStocksByCreator(@RequestParam String createdBy){

        return ResponseEntity.ok(returnedStockServices.fetchAllUnapprovedReturnsCreatedBy(createdBy));
    }

    @GetMapping(path = "/all/returnedWithin")
    public ResponseEntity<?> fetchAllReturnedStocksWithin(@RequestParam Date from,
                                                          @RequestParam Date to){

        return ResponseEntity.ok(returnedStockServices.fetchAllReturnsWithin(from, to));
    }

    @GetMapping(path = "/all/unapproved")
    public ResponseEntity<?> fetchAllUnApprovedReturnedStocks(){

        return ResponseEntity.ok(returnedStockServices.fetchAllUnApprovedReturnSales());
    }

    @PutMapping(path = "/approve")
    public ResponseEntity<?> approveReturnedStock(@RequestParam Long returnedStockId){

        ReturnedStock returnedStock = returnedStockServices.approveReturnSales(returnedStockId);

        if (null == returnedStock) throw new InventoryAPIOperationException("Not approved",
                "The returned stock was not approved successfully, review inputs and try again", null);

        return ResponseEntity.ok(returnedStock);
    }

    @DeleteMapping(path = "/byId")
    public ResponseEntity<?> deleteReturnedStockById(@RequestParam Long returnedStockId){

        ReturnedStock returnedStock = returnedStockServices.deleteReturnedStock(returnedStockId);

        if (null == returnedStock) throw new InventoryAPIOperationException("Delete failed",
                "Deleting returned stock was not successful, review your inputs and try again", null);

        return ResponseEntity.ok(returnedStock);
    }
}
