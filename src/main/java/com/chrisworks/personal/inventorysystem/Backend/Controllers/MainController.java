package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.EXPENSE_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.INCOME_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDataValidationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Services.*;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;

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

    private final ShopServices shopServices;

    private final CustomerService customerService;

    private final StockServices stockServices;

    private final InvoiceServices invoiceServices;

    @Autowired
    public MainController(GenericService genericService, CustomerService customerService,
                          ShopServices shopServices, StockServices stockServices,
                          InvoiceServices invoiceServices) {
        this.genericService = genericService;
        this.customerService = customerService;
        this.shopServices = shopServices;
        this.stockServices = stockServices;
        this.invoiceServices = invoiceServices;
    }

    @PostMapping(path = "customer", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addCustomer(@RequestBody @Valid Customer customer){

        preAuthorizeLoggedInUser();

        Customer newCustomer = genericService.addCustomer(customer);

        if (newCustomer == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + customer, null);

        return new ResponseEntity<>(newCustomer, HttpStatus.CREATED);
    }

    @PostMapping(path = "supplier", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addSupplier(@RequestBody @Valid Supplier supplier){

        preAuthorizeLoggedInUser();

        Supplier newSupplier = genericService.addSupplier(supplier);

        if (newSupplier == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + supplier, null);

        return new ResponseEntity<>(newSupplier, HttpStatus.CREATED);
    }

    @PostMapping(path = "stock/category", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addStockCategory(@RequestBody @Valid StockCategory stockCategory){

        preAuthorizeLoggedInUser();

        if (ACCOUNT_TYPE.SELLER.equals(AuthenticatedUserDetails.getAccount_type())) throw new InventoryAPIOperationException
                ("Operation not allowed", "Logged in user cannot perform this operation", null);

        StockCategory newStockCategory = genericService.addStockCategory(stockCategory);

        if (newStockCategory == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + stockCategory, null);

        return new ResponseEntity<>(newStockCategory, HttpStatus.CREATED);
    }

    @PostMapping(path = "stock", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addStock(@RequestParam Long warehouseId, @RequestBody @Valid Stock stock){

        preAuthorizeLoggedInUser();

        Stock newStock = genericService.addStock(warehouseId, stock);

        if (newStock == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + stock, null);

        return new ResponseEntity<>(newStock, HttpStatus.CREATED);
    }

    @PutMapping(path = "restock", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> reStock(@RequestParam Long stockId, @RequestParam Long warehouseId,
                                     @RequestBody @Valid Stock stock){

        preAuthorizeLoggedInUser();

        Stock reStock = genericService.reStock(warehouseId, stockId, stock);

        if (reStock == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + stock, null);

        return new ResponseEntity<>(reStock, HttpStatus.CREATED);
    }

    //Not tested yet
    @PostMapping(path = "sell", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> sellStock(@RequestBody @Valid Invoice invoice){

        preAuthorizeLoggedInUser();

        Invoice newInvoice = genericService.sellStock(invoice);

        if (newInvoice == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + invoice, null);

        return ResponseEntity.ok(newInvoice);
    }

    //Not tested yet
    @PostMapping(path = "return", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> processReturn(@RequestBody @Valid ReturnedStock returnedStock){

        preAuthorizeLoggedInUser();

        ReturnedStock newReturnedStock = genericService.processReturn(returnedStock);

        if (newReturnedStock == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + returnedStock, null);

        return ResponseEntity.ok(newReturnedStock);
    }

    @PostMapping(path = "list/return", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> processReturnList(@RequestBody @Valid List<ReturnedStock> returnedStockList){

        preAuthorizeLoggedInUser();

        List<ReturnedStock> newReturnedStockList = genericService.processReturnList(returnedStockList);

        if (newReturnedStockList == null) throw new InventoryAPIOperationException
                ("List not saved", "Could not save the list of entities: " + returnedStockList, null);

        return (!newReturnedStockList.isEmpty()) ? ResponseEntity.ok(newReturnedStockList)
                : ResponseEntity.noContent().build();
    }

    @PostMapping(path = "expense", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addExpense(@RequestBody @Valid Expense expense){

        preAuthorizeLoggedInUser();

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

        preAuthorizeLoggedInUser();

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

        preAuthorizeLoggedInUser();

        Stock stockWithNewSellingPrice = genericService.changeStockSellingPriceById(stockId, newSellingPrice);

        if (stockWithNewSellingPrice == null) throw new InventoryAPIOperationException
                ("Data not updated", "Could not update entity with value: " + newSellingPrice.toString(), null);

        return ResponseEntity.ok(stockWithNewSellingPrice);
    }

    @PutMapping(path = "change/selling/price/stockName")
    public ResponseEntity<?> changeStockSellingPriceByName(@RequestParam Long warehouseId, @RequestParam String stockName,
                                                           @RequestParam BigDecimal newSellingPrice){

        preAuthorizeLoggedInUser();

        Stock stockWithNewSellingPrice = genericService
                .changeStockSellingPriceByWarehouseIdAndStockName(warehouseId, stockName, newSellingPrice);

        if (stockWithNewSellingPrice == null) throw new InventoryAPIOperationException
                ("Data not updated", "Could not update entity with value: " + newSellingPrice.toString(), null);

        return ResponseEntity.ok(stockWithNewSellingPrice);
    }

    //Get all customers from invoices or directly created by his seller or him (business owner)
    @GetMapping(path = "customers")
    public ResponseEntity<?> fetchAllCustomers(){

        preAuthorizeLoggedInUser();

        Set<Customer> customersList = genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getWarehouseId)
                .map(shopServices::fetchAllShopInWarehouse)
                .flatMap(List::parallelStream)
                .map(Shop::getShopId)
                .map(customerService::fetchCustomersByShop)
                .flatMap(List::parallelStream)
                .collect(Collectors.toSet());

        customersList.addAll(customerService.fetchAllCustomersByCreator(AuthenticatedUserDetails.getUserFullName()));

        return ResponseEntity.ok(customersList);
    }

    //Get all stock category, using created by
    @GetMapping(path = "stockCategory")
    public ResponseEntity<?> fetchAllStockCategory(){

        preAuthorizeLoggedInUser();

        List<StockCategory> stockCategoryList = genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getBusinessOwner)
                .map(BusinessOwner::getBusinessOwnerEmail)
                .map(genericService::fetchAllStockCategoryByCreator)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());

        return ResponseEntity.ok(stockCategoryList);
    }

    //Get all supplier, using created by
    @GetMapping(path = "suppliers")
    public ResponseEntity<?> fetchAllSuppliers(){

        preAuthorizeLoggedInUser();

        Set<Supplier> supplierList = genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getWarehouseId)
                .map(stockServices::allStockByWarehouseId)
                .flatMap(List::parallelStream)
                .map(Stock::getStockPurchasedFrom)
                .flatMap(Set::parallelStream)
                .collect(Collectors.toSet());

        supplierList.addAll(genericService.fetchSuppliersByCreator(AuthenticatedUserDetails.getUserFullName()));

        return ResponseEntity.ok(supplierList);
    }

    //Get invoice by invoiceNumber
    @GetMapping(path = "invoice/number")
    public ResponseEntity<?> getInvoiceByNumber(@RequestParam String invoiceNumber){

        preAuthorizeLoggedInUser();

            Invoice invoiceFound = findInvoiceByNumber(invoiceNumber);

            return ResponseEntity.ok(invoiceFound);
    }

    //Soon to finish stock
    @GetMapping(path = "finishing/stock")
    public ResponseEntity<?> getSoonToFinishStock(@RequestParam int limit){

        preAuthorizeLoggedInUser();

        List<Stock> soonToFinishStock = genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getWarehouseId)
                .map(warehouseId -> stockServices.allSoonToFinishStock(warehouseId, limit))
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());

        return ResponseEntity.ok(soonToFinishStock);
    }

    //Soon to finish stock
    @GetMapping(path = "expiring/stock")
    public ResponseEntity<?> getSoonToExpireStock(){

        preAuthorizeLoggedInUser();

        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, 60);
        date = c.getTime();

        Date finalDate = date;

        List<Stock> soonToExpireStock = genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getWarehouseId)
                .map(warehouseId -> stockServices.allSoonToExpireStock(warehouseId, finalDate))
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());

        return ResponseEntity.ok(soonToExpireStock);
    }

    //Put request, clearDebt
    @PutMapping(path = "clearDebt")
    public ResponseEntity<?> clearDebt(@RequestParam String invoiceNumber, @RequestParam BigDecimal amountPaid){

        preAuthorizeLoggedInUser();

        findInvoiceByNumber(invoiceNumber);

        Invoice invoiceRetrieved = invoiceServices.clearDebt(invoiceNumber, amountPaid);

        return ResponseEntity.ok(invoiceRetrieved);
    }

    //Retrieve invoice using the invoice number
    private Invoice findInvoiceByNumber(String invoiceNumber) {

        Set<Invoice> invoiceList = genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getWarehouseId)
                .map(shopServices::fetchAllShopInWarehouse)
                .flatMap(List::parallelStream)
                .map(Shop::getSellers)
                .flatMap(Set::parallelStream)
                .map(Seller::getInvoices)
                .flatMap(Set::parallelStream)
                .collect(Collectors.toSet());

        if (ACCOUNT_TYPE.BUSINESS_OWNER.equals(AuthenticatedUserDetails.getAccount_type()))
            invoiceList.addAll(invoiceServices.fetchAllInvoicesCreatedBy(AuthenticatedUserDetails.getUserFullName()));

        Invoice invoiceFound = invoiceList.stream()
                .filter(invoice -> invoice.getInvoiceNumber().equalsIgnoreCase(invoiceNumber))
                .collect(toSingleton());

        if (null == invoiceFound && ACCOUNT_TYPE.BUSINESS_OWNER.equals(AuthenticatedUserDetails.getAccount_type()))
            throw new InventoryAPIResourceNotFoundException("Invoice not found", "No invoice with invoice number: "
                    + invoiceNumber + " was found in any of your stores or your personal sales", null);

        if (null == invoiceFound && ACCOUNT_TYPE.SELLER.equals(AuthenticatedUserDetails.getAccount_type()))
            throw new InventoryAPIResourceNotFoundException("Invoice not found", "No invoice with invoice number: "
                    + invoiceNumber + " was found in this shop, contact the business owner to further verify", null);

        return invoiceFound;
    }

    //Verify that request comes from account type BUSINESS_OWNER or SELLER
    private void preAuthorizeLoggedInUser(){

        if (null == AuthenticatedUserDetails.getAccount_type()) throw new InventoryAPIOperationException
                ("Unknown user", "Logged in user is unknown and cannot proceed with any operation in the system", null);
    }
}
