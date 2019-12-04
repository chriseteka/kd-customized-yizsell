package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.Services.GenericServices.GenericService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
@Validated
@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MainController {

    private final GenericService genericService;

    @Autowired
    public MainController(GenericService genericService) {
        this.genericService = genericService;
    }

    @PostMapping(path = "customer", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addCustomer(@RequestBody @Valid Customer customer){

        Customer newCustomer = genericService.addCustomer(customer);

        //Exception thrown here should be modified.

        return newCustomer != null ? ResponseEntity.ok(newCustomer) : ResponseEntity.noContent().build();
    }

    @PostMapping(path = "supplier", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addSupplier(@RequestBody @Valid Supplier supplier){

        Supplier newSupplier = genericService.addSupplier(supplier);

        //Exception thrown here should be modified.

        return newSupplier != null ? ResponseEntity.ok(newSupplier) : ResponseEntity.noContent().build();
    }

    @PostMapping(path = "stock", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addStock(@RequestBody @Valid Stock stock, @RequestBody @Valid Supplier supplier){

        Stock newStock = genericService.addStock(stock, supplier);

        //Exception thrown here should be modified.

        return newStock != null ? ResponseEntity.ok(newStock) : ResponseEntity.noContent().build();
    }

    @PostMapping(path = "restock", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> reStock(@RequestParam Long stockId, @RequestBody @Valid Stock stock,
                                     @RequestBody @Valid Supplier supplier){

        Stock reStock = genericService.reStock(stockId, stock, supplier);

        //Exception thrown here should be modified.

        return reStock != null ? ResponseEntity.ok(reStock) : ResponseEntity.noContent().build();
    }

    @PostMapping(path = "sell", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> sellStock(@RequestBody @Valid Invoice invoice){

        Invoice newInvoice = genericService.sellStock(invoice);

        //Exception thrown here should be modified.

        return newInvoice != null ? ResponseEntity.ok(newInvoice) : ResponseEntity.noContent().build();
    }

    @PostMapping(path = "return", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> processReturn(@RequestBody @Valid ReturnedStock returnedStock){

        ReturnedStock newReturnedStock = genericService.processReturn(returnedStock);

        //Exception thrown here should be modified.

        return newReturnedStock != null ? ResponseEntity.ok(newReturnedStock) : ResponseEntity.noContent().build();
    }


    @PostMapping(path = "list/return", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> processReturnList(@RequestBody @Valid List<ReturnedStock> returnedStockList){

        List<ReturnedStock> newReturnedStockList = genericService.processReturnList(returnedStockList);

        //Exception thrown here should be modified.

        return (newReturnedStockList != null && !newReturnedStockList.isEmpty()) ?
                ResponseEntity.ok(newReturnedStockList) : ResponseEntity.noContent().build();
    }

    @PostMapping(path = "expense", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addExpense(@RequestBody @Valid Expense expense){

        Expense newExpense = genericService.addExpense(expense);

        //Exception thrown here should be modified.

        return newExpense != null ? ResponseEntity.ok(newExpense) : ResponseEntity.noContent().build();
    }

    @PostMapping(path = "income", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addIncome(@RequestBody @Valid Income income){

        Income newIncome = genericService.addIncome(income);

        //Exception thrown here should be modified.

        return newIncome != null ? ResponseEntity.ok(newIncome) : ResponseEntity.noContent().build();
    }

    @GetMapping(path = "change/selling/price/stockId")
    public ResponseEntity<?> changeStockSellingPriceById(@RequestParam Long stockId, @RequestParam BigDecimal newSellingPrice){

        Stock stockWithNewSellingPrice = genericService.changeStockSellingPriceById(stockId, newSellingPrice);

        //Exception thrown here should be modified.

        return stockWithNewSellingPrice != null ?
                ResponseEntity.ok(stockWithNewSellingPrice) : ResponseEntity.noContent().build();
    }

    @GetMapping(path = "change/selling/price/stockName")
    public ResponseEntity<?> changeStockSellingPriceByName(@RequestParam String stockName,
                                                           @RequestParam BigDecimal newSellingPrice){

        Stock stockWithNewSellingPrice = genericService.changeStockSellingPriceByName(stockName, newSellingPrice);

        //Exception thrown here should be modified.

        return stockWithNewSellingPrice != null ?
                ResponseEntity.ok(stockWithNewSellingPrice) : ResponseEntity.noContent().build();
    }

}
