package com.chrisworks.personal.inventorysystem.Backend.Controllers.BusinessOwnerController;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Services.*;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;

/**
 * @author Chris_Eteka
 * @since 11/27/2019
 * @email chriseteka@gmail.com
 */
@RestController
@RequestMapping("/BO")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BusinessOwnerController {

    private BusinessOwnerServices businessOwnerServices;

    private GenericService genericService;

    private ShopServices shopServices;

    private SellerServices sellerServices;

    private WarehouseServices warehouseServices;

    private StockServices stockServices;

    private IncomeServices incomeServices;

    private ExpenseServices expenseServices;

    private ReturnedStockServices returnedStockServices;

    private InvoiceServices invoiceServices;

    @Autowired
    public BusinessOwnerController(BusinessOwnerServices businessOwnerServices, GenericService genericService,
                                   ShopServices shopServices, SellerServices sellerServices,
                                   WarehouseServices warehouseServices, StockServices stockServices,
                                   IncomeServices incomeServices, ExpenseServices expenseServices,
                                   ReturnedStockServices returnedStockServices, InvoiceServices invoiceServices) {
        this.businessOwnerServices = businessOwnerServices;
        this.genericService = genericService;
        this.shopServices = shopServices;
        this.sellerServices = sellerServices;
        this.warehouseServices = warehouseServices;
        this.stockServices = stockServices;
        this.incomeServices = incomeServices;
        this.expenseServices = expenseServices;
        this.returnedStockServices = returnedStockServices;
        this.invoiceServices = invoiceServices;
    }

    @PostMapping(path = "/createAccount", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createAccount(@RequestBody @Valid BusinessOwner businessOwner){

        BusinessOwner businessOwnerAccount = businessOwnerServices.createAccount(businessOwner);

        if (businessOwnerAccount == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + businessOwner, null);

        return new ResponseEntity<>(businessOwnerAccount, HttpStatus.CREATED);
    }

    //Not tested
    @PostMapping(path = "/updateAccount/id", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateProfile(@RequestBody @Valid BusinessOwner businessOwner,
                                           @RequestParam Long businessOwnerId){

        BusinessOwner updatedAccount = businessOwnerServices.updateAccount(businessOwnerId, businessOwner);

        if (updatedAccount == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not update entity: " + businessOwner, null);

        return new ResponseEntity<>(updatedAccount, HttpStatus.OK);
    }

    @GetMapping(path = "/sellerShop")
    public ResponseEntity<?> shopBySeller(@RequestParam String sellerName){

        Shop shopRetrieved = genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getWarehouseId)
                .map(shopServices::fetchAllShopInWarehouse)
                .flatMap(List::stream)
                .filter(shop -> shop.getSellers()
                        .stream()
                        .map(seller -> seller.getSellerFullName().equalsIgnoreCase(sellerName))
                        .collect(toSingleton()))
                .collect(toSingleton());

//        Shop shopRetrieved = genericService.shopBySellerName(sellerName);

        return shopRetrieved != null ? ResponseEntity.ok(shopRetrieved) : ResponseEntity.notFound().build();
    }

    @PostMapping(path = "/addWarehouse", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addWarehouse(@RequestBody @Valid Warehouse warehouse, @RequestParam Long businessOwnerId){

        Warehouse warehouseAdded = warehouseServices.addWarehouse(businessOwnerId, warehouse);

        if (warehouseAdded == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + warehouse, null);

        return new ResponseEntity<>(warehouseAdded, HttpStatus.CREATED);
    }

    @PostMapping(path = "/addShop", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addShop(@RequestBody @Valid Shop shop, @RequestParam Long warehouseId){

        Warehouse warehouseById = warehouseServices.warehouseById(warehouseId);

        if (warehouseById == null)throw new InventoryAPIOperationException
                ("Data not saved", "Could not find a warehouse with the id: " + warehouseId, null);

        Warehouse warehouseFound = genericService.allWarehouseByAuthUserId()
                .stream()
                .filter(warehouse -> warehouse.equals(warehouseById))
                .collect(toSingleton());

        if (warehouseFound == null)throw new InventoryAPIOperationException
                ("Data not saved", "Could not find a warehouse with the id: " + warehouseId + " in your list of warehouses", null);

        Shop shopAdded = shopServices.addShop(warehouseFound, shop);

        if (shopAdded == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + shop, null);

        return new ResponseEntity<>(shopAdded, HttpStatus.CREATED);
    }

    @PostMapping(path = "/addSeller", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addSeller(@RequestParam Long shopId, @RequestBody @Valid Seller seller){

        Shop shopById = shopServices.findShopById(shopId);

        if (shopById == null)throw new InventoryAPIOperationException
                ("Data not saved", "Could not find a shop with the id: " + shopId, null);

        Shop shopFound = genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getWarehouseId)
                .map(shopServices::fetchAllShopInWarehouse)
                .flatMap(List::stream)
                .filter(shop -> shop.equals(shopById))
                .collect(toSingleton());

        if (shopFound == null)throw new InventoryAPIOperationException
                ("Data not saved", "Could not find a shop with the id: " + shopId + " in your list of shops", null);

        Seller sellerCreated = sellerServices.createSeller(seller);

        Shop shop = shopServices.addSellerToShop(shopFound, sellerCreated);

        if (shop == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + seller + " to" +
                    " shop with id: " + shopId, null);

        return new ResponseEntity<>(shop, HttpStatus.OK);
    }

    //Get all warehouses
    @GetMapping(path = "/warehouses")
    public ResponseEntity<?> fetchAllWarehouses(){

        return ResponseEntity.ok(genericService.allWarehouseByAuthUserId());
    }

    //Get all shops
    @GetMapping(path = "/shops")
    public ResponseEntity<?> fetchAllShops(){

        List<Shop> shopList = genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getWarehouseId)
                .map(shopServices::fetchAllShopInWarehouse)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());

        return ResponseEntity.ok(shopList);
    }

    //Get all Sellers
    @GetMapping(path = "/sellers")
    public ResponseEntity<?> fetchAllSellers(){

        List<Seller> sellerList = genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getWarehouseId)
                .map(shopServices::fetchAllShopInWarehouse)
                .flatMap(List::parallelStream)
                .map(Shop::getSellers)
                .flatMap(Set::parallelStream)
                .collect(Collectors.toList());

        return ResponseEntity.ok(sellerList);
    }

    //Get all stock
    @GetMapping(path = "/stock")
    public ResponseEntity<?> fetchAllStock(){

        List<Stock> stockList = genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getWarehouseId)
                .map(stockServices::allStockByWarehouseId)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());

        return ResponseEntity.ok(stockList);
    }

    //Get all income
    @GetMapping(path = "/income")
    public ResponseEntity<?> fetchAllIncome(){

        List<Income> incomeList = genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getWarehouseId)
                .map(shopServices::fetchAllShopInWarehouse)
                .flatMap(List::parallelStream)
                .map(Shop::getIncome)
                .flatMap(Set::parallelStream)
                .collect(Collectors.toList());
        if (ACCOUNT_TYPE.BUSINESS_OWNER.equals(AuthenticatedUserDetails.getAccount_type()))
            incomeList.addAll(incomeServices.fetchIncomeCreatedBy(AuthenticatedUserDetails.getUserFullName()));

        return ResponseEntity.ok(incomeList);
    }

    //Get all expenses
    @GetMapping(path = "/expenses")
    public ResponseEntity<?> fetchAllExpenses(){

        List<Expense> expenseList = genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getWarehouseId)
                .map(shopServices::fetchAllShopInWarehouse)
                .flatMap(List::parallelStream)
                .map(Shop::getExpenses)
                .flatMap(Set::parallelStream)
                .collect(Collectors.toList());
        if (ACCOUNT_TYPE.BUSINESS_OWNER.equals(AuthenticatedUserDetails.getAccount_type()))
            expenseList.addAll(expenseServices.fetchExpensesCreatedBy(AuthenticatedUserDetails.getUserFullName()));

        return ResponseEntity.ok(expenseList);
    }

    //Get all returned stock
    @GetMapping(path = "/return/sales")
    public ResponseEntity<?> fetchAllReturnSales(){

        List<ReturnedStock> returnedStocks = genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getWarehouseId)
                .map(shopServices::fetchAllShopInWarehouse)
                .flatMap(List::parallelStream)
                .map(Shop::getReturnedSales)
                .flatMap(Set::parallelStream)
                .collect(Collectors.toList());
        if (ACCOUNT_TYPE.BUSINESS_OWNER.equals(AuthenticatedUserDetails.getAccount_type()))
            returnedStocks.addAll(returnedStockServices.fetchAllStockReturnedTo(AuthenticatedUserDetails.getUserFullName()));

        return ResponseEntity.ok(returnedStocks);
    }

    //Get all invoices
    @GetMapping(path = "/invoices")
    public ResponseEntity<?> fetchAllInvoices(){

        List<Invoice> invoiceList = genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getWarehouseId)
                .map(shopServices::fetchAllShopInWarehouse)
                .flatMap(List::parallelStream)
                .map(Shop::getSellers)
                .flatMap(Set::parallelStream)
                .map(Seller::getInvoices)
                .flatMap(Set::parallelStream)
                .collect(Collectors.toList());
        if (ACCOUNT_TYPE.BUSINESS_OWNER.equals(AuthenticatedUserDetails.getAccount_type()))
            invoiceList.addAll(invoiceServices.fetchAllInvoicesCreatedBy(AuthenticatedUserDetails.getUserFullName()));

        return ResponseEntity.ok(invoiceList);
    }

    //Get all stock sold from the invoices
    @GetMapping(path = "/stock/sold")
    public ResponseEntity<?> fetchAllStockSold(){

        List<StockSold> stockSoldList = genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getWarehouseId)
                .map(shopServices::fetchAllShopInWarehouse)
                .flatMap(List::parallelStream)
                .map(Shop::getSellers)
                .flatMap(Set::parallelStream)
                .map(Seller::getInvoices)
                .flatMap(Set::parallelStream)
                .map(Invoice::getStockSold)
                .flatMap(Set::parallelStream)
                .collect(Collectors.toList());

        if (ACCOUNT_TYPE.BUSINESS_OWNER.equals(AuthenticatedUserDetails.getAccount_type()))
            stockSoldList.addAll(invoiceServices
                    .fetchAllInvoicesCreatedBy(AuthenticatedUserDetails.getUserFullName())
                    .stream()
                    .map(Invoice::getStockSold)
                    .flatMap(Set::parallelStream)
                    .collect(Collectors.toList()));

        return ResponseEntity.ok(stockSoldList);
    }

    //Get all unapproved stock
    @GetMapping(path = "/stock/unapproved")
    public ResponseEntity<?> fetchAllUnapprovedStock(){

        List<Stock> unApprovedStockList = genericService.allWarehouseByAuthUserId()
                .parallelStream()
                .map(Warehouse::getWarehouseId)
                .map(stockServices::unApprovedStock)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());

        if (ACCOUNT_TYPE.SELLER.equals(AuthenticatedUserDetails.getAccount_type()))
            unApprovedStockList.addAll(stockServices.unApprovedStockByCreator(AuthenticatedUserDetails.getUserFullName()));

        return ResponseEntity.ok(unApprovedStockList);
    }

    //Get all unapproved income
    @GetMapping(path = "/income/unapproved")
    public ResponseEntity<?> fetchAllUnapprovedIncome(){

        List<Income> unApprovedIncomeList = shopServices.allUnApprovedIncome();

        if (ACCOUNT_TYPE.SELLER.equals(AuthenticatedUserDetails.getAccount_type()))
            unApprovedIncomeList.addAll(incomeServices
                    .fetchAllUnApprovedIncomeByCreator(AuthenticatedUserDetails.getUserFullName()));

        return ResponseEntity.ok(unApprovedIncomeList);
    }

    //Get all unapproved expense
    @GetMapping(path = "/expense/unapproved")
    public ResponseEntity<?> fetchAllUnapprovedExpense(){

        List<Expense> allUnApprovedExpense = shopServices.allUnApprovedExpense();

        if (ACCOUNT_TYPE.SELLER.equals(AuthenticatedUserDetails.getAccount_type()))
            allUnApprovedExpense.addAll(expenseServices
                    .fetchAllUnApprovedExpensesCreatedBy(AuthenticatedUserDetails.getUserFullName()));

        return ResponseEntity.ok(allUnApprovedExpense);
    }

    //Get all unapproved returned sales
    @GetMapping(path = "/return/sales/unapproved")
    public ResponseEntity<?> fetchAllUnapprovedReturns(){

        List<ReturnedStock> returnedStockList = shopServices.allUnApprovedReturnSales();

        if (ACCOUNT_TYPE.SELLER.equals(AuthenticatedUserDetails.getAccount_type()))
            returnedStockList.addAll(returnedStockServices
                    .fetchAllUnapprovedReturnsCreatedBy(AuthenticatedUserDetails.getUserFullName()));

        return ResponseEntity.ok(returnedStockList);
    }

    //Put request, approve stock
    @PutMapping(path = "/approve/stock")
    public ResponseEntity<?> approveStock(@RequestParam Long stockId){

        Stock approvedStock = stockServices.approveStock(stockId);

        if (null == approvedStock) throw new InventoryAPIOperationException
                ("Stock not approved", "Stock not approved", null);

        return ResponseEntity.ok(approvedStock);
    }

    //Put request, approve stock list
    @PutMapping(path = "/approve/stockList")
    public ResponseEntity<?> approveStockList(@RequestParam List<Long> stockIds){

        List<Stock> stockList = stockServices.approveStockList(stockIds);

        if (null == stockList || stockList.isEmpty()) throw new InventoryAPIOperationException
                ("stock list not approved", "stock list not approved", null);

        return ResponseEntity.ok(stockList);
    }

    //Put request, approve income
    @PutMapping(path = "/approve/income")
    public ResponseEntity<?> approveIncome(@RequestParam Long incomeId){

        Income approvedIncome = shopServices.approveIncome(incomeId);

        if (null == approvedIncome) throw new InventoryAPIOperationException
                ("Income not approved", "Income not approved", null);

        return ResponseEntity.ok(approvedIncome);
    }

    //Put request, approve expense
    @PutMapping(path = "/approve/expense")
    public ResponseEntity<?> approveExpense(@RequestParam Long expenseId){

        Expense approvedExpense = shopServices.approveExpense(expenseId);

        if (null == approvedExpense) throw new InventoryAPIOperationException
                ("Expense not approved", "Expense not approved", null);

        return ResponseEntity.ok(approvedExpense);
    }

    //Put request, approve returns
    @PutMapping(path = "/approve/return/sale")
    public ResponseEntity<?> approveReturnSale(@RequestParam Long returnSaleId){

        ReturnedStock returnedStock = shopServices.approveReturnSales(returnSaleId);

        if (null == returnedStock) throw new InventoryAPIOperationException
                ("Returned stock not approved", "Returned stock not approved", null);

        return ResponseEntity.ok(returnedStock);
    }

    //Delete request in pairs, for stock, seller, warehouse, income, expense, shop, stock category, returns, invoice,
    //supplier, customer
}
