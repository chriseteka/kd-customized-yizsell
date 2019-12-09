package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.EXPENSE_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.INCOME_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDataValidationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Services.GenericService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

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

        if (newCustomer == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + customer, null);

        return new ResponseEntity<>(newCustomer, HttpStatus.CREATED);
    }

    @PostMapping(path = "supplier", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addSupplier(@RequestBody @Valid Supplier supplier){

        Supplier newSupplier = genericService.addSupplier(supplier);

        if (newSupplier == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + supplier, null);

        return new ResponseEntity<>(newSupplier, HttpStatus.CREATED);
    }

    @PostMapping(path = "stock/category", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addStockCategory(@RequestBody @Valid StockCategory stockCategory){

        StockCategory newStockCategory = genericService.addStockCategory(stockCategory);

        if (newStockCategory == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + stockCategory, null);

        return new ResponseEntity<>(newStockCategory, HttpStatus.CREATED);
    }

    @PostMapping(path = "stock", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addStock(@RequestParam Long warehouseId, @RequestBody @Valid Stock stock){

        Stock newStock = genericService.addStock(warehouseId, stock);

        if (newStock == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + stock, null);

        return new ResponseEntity<>(newStock, HttpStatus.CREATED);
    }

    @PutMapping(path = "restock", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> reStock(@RequestParam Long stockId, @RequestParam Long warehouseId,
                                     @RequestBody @Valid Stock stock){

        Stock reStock = genericService.reStock(warehouseId, stockId, stock);

        if (reStock == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + stock, null);

        return new ResponseEntity<>(reStock, HttpStatus.CREATED);
    }

    //Not tested yet
    @PostMapping(path = "sell", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> sellStock(@RequestBody @Valid Invoice invoice){

        Invoice newInvoice = genericService.sellStock(invoice);

        if (newInvoice == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + invoice, null);

        return ResponseEntity.ok(newInvoice);
    }

    //Not tested yet
    @PostMapping(path = "return", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> processReturn(@RequestBody @Valid ReturnedStock returnedStock){

        ReturnedStock newReturnedStock = genericService.processReturn(returnedStock);

        if (newReturnedStock == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + returnedStock, null);

        return ResponseEntity.ok(newReturnedStock);
    }

    @PostMapping(path = "list/return", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> processReturnList(@RequestBody @Valid List<ReturnedStock> returnedStockList){

        List<ReturnedStock> newReturnedStockList = genericService.processReturnList(returnedStockList);

        if (newReturnedStockList == null) throw new InventoryAPIOperationException
                ("List not saved", "Could not save the list of entities: " + returnedStockList, null);

        return (!newReturnedStockList.isEmpty()) ? ResponseEntity.ok(newReturnedStockList)
                : ResponseEntity.noContent().build();
    }

    @PostMapping(path = "expense", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addExpense(@RequestBody @Valid Expense expense){

        if (!expense.getExpenseTypeVal().matches("\\d+")) throw new InventoryAPIDataValidationException
                ("Expense Type value error", "Expense Type value must be any of these: 100, 200, 300, 400", null);

        expense.setExpenseTypeValue(Integer.parseInt(expense.getExpenseTypeVal()));

        IntStream incomeValueStream = Arrays.stream(EXPENSE_TYPE.values()).mapToInt(EXPENSE_TYPE::getExpense_type_value);

        if (incomeValueStream.noneMatch(value -> value == expense.getExpenseTypeValue()))
            throw new InventoryAPIDataValidationException("Income Type value error", "Income Type value must be any of these: 100, 200, 300, 400", null);

        Expense newExpense = genericService.addExpense(expense);

        if (newExpense == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + expense, null);

        return new ResponseEntity<>(newExpense, HttpStatus.CREATED);
    }

    @PostMapping(path = "income", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addIncome(@RequestBody @Valid Income income){

        if (!income.getIncomeTypeVal().matches("\\d+"))
            throw new InventoryAPIDataValidationException("Income Type value error", "Income Type value must be any of these: 100, 200, 300", null);

        income.setIncomeTypeValue(Integer.parseInt(income.getIncomeTypeVal()));

        IntStream incomeValueStream = Arrays.stream(INCOME_TYPE.values()).mapToInt(INCOME_TYPE::getIncome_type_value);

        if (incomeValueStream.noneMatch(value -> value == income.getIncomeTypeValue())) throw new
                InventoryAPIDataValidationException("Income Type value error", "Income Type value must be any of these: 100, 200, 300", null);

        Income newIncome = genericService.addIncome(income);

        if (newIncome == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + income, null);

        return new ResponseEntity<>(newIncome, HttpStatus.CREATED);
    }

    @PutMapping(path = "change/selling/price/stockId")
    public ResponseEntity<?> changeStockSellingPriceById(@RequestParam Long stockId,
                                                         @RequestParam BigDecimal newSellingPrice){

        Stock stockWithNewSellingPrice = genericService.changeStockSellingPriceById(stockId, newSellingPrice);

        if (stockWithNewSellingPrice == null) throw new InventoryAPIOperationException
                ("Data not updated", "Could not update entity with value: " + newSellingPrice.toString(), null);

        return ResponseEntity.ok(stockWithNewSellingPrice);
    }

    @PutMapping(path = "change/selling/price/stockName")
    public ResponseEntity<?> changeStockSellingPriceByName(@RequestParam Long warehouseId, @RequestParam String stockName,
                                                           @RequestParam BigDecimal newSellingPrice){

        Stock stockWithNewSellingPrice = genericService
                .changeStockSellingPriceByWarehouseIdAndStockName(warehouseId, stockName, newSellingPrice);

        if (stockWithNewSellingPrice == null) throw new InventoryAPIOperationException
                ("Data not updated", "Could not update entity with value: " + newSellingPrice.toString(), null);

        return ResponseEntity.ok(stockWithNewSellingPrice);
    }
}
