//package com.chrisworks.personal.inventorysystem.Backend.Controllers;
//
//import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
//import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
//import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
//import com.chrisworks.personal.inventorysystem.Backend.Services.*;
//import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.context.request.WebRequest;
//
//import javax.validation.Valid;
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;
//import static ir.cafebabe.math.utils.BigDecimalUtils.is;
//
///**
// * @author Chris_Eteka
// * @since 11/27/2019
// * @email chriseteka@gmail.com
// */
//@RestController
//@RequestMapping("/BO")
//@CrossOrigin(origins = "*", allowedHeaders = "*")
//public class BusinessOwnerController {
//
//    private BusinessOwnerServices businessOwnerServices;
//
//    private GenericService genericService;
//
//    private ShopServices shopServices;
//
//    private SellerServices sellerServices;
//
//    private WarehouseServices warehouseServices;
//
//    private StockServices stockServices;
//
//    private IncomeServices incomeServices;
//
//    private ExpenseServices expenseServices;
//
//    private ReturnedStockServices returnedStockServices;
//
//    private InvoiceServices invoiceServices;
//
//    private CustomerService customerService;
//
//    @Autowired
//    public BusinessOwnerController(BusinessOwnerServices businessOwnerServices, GenericService genericService,
//                                   ShopServices shopServices, SellerServices sellerServices,
//                                   WarehouseServices warehouseServices, StockServices stockServices,
//                                   IncomeServices incomeServices, ExpenseServices expenseServices,
//                                   ReturnedStockServices returnedStockServices, InvoiceServices invoiceServices,
//                                   CustomerService customerService) {
//        this.businessOwnerServices = businessOwnerServices;
//        this.genericService = genericService;
//        this.shopServices = shopServices;
//        this.sellerServices = sellerServices;
//        this.warehouseServices = warehouseServices;
//        this.stockServices = stockServices;
//        this.incomeServices = incomeServices;
//        this.expenseServices = expenseServices;
//        this.returnedStockServices = returnedStockServices;
//        this.invoiceServices = invoiceServices;
//        this.customerService = customerService;
//    }
//
//    @GetMapping(path = "/sellerShop")
//    public ResponseEntity<?> shopBySeller(@RequestParam String seller){
//
//        preAuthorizeBusinessOwner();
//
//        Shop shopRetrieved = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(shopServices::fetchAllShopInWarehouse)
//                .flatMap(List::stream)
//                .map(sellerServices::fetchShopSellersByShop)
//                .flatMap(List::stream)
//                .filter(sellerFound -> sellerFound.getSellerFullName().equalsIgnoreCase(seller)
//                        || sellerFound.getSellerEmail().equalsIgnoreCase(seller))
//                .collect(toSingleton())
//                .getShop();
//
//        return shopRetrieved != null ? ResponseEntity.ok(shopRetrieved) : ResponseEntity.notFound().build();
//    }
//
//    @PostMapping(path = "/addWarehouse", consumes = "application/json", produces = "application/json")
//    public ResponseEntity<?> addWarehouse(@RequestBody @Valid Warehouse warehouse, @RequestParam Long businessOwnerId){
//
//        preAuthorizeBusinessOwner();
//
//        Warehouse warehouseAdded = warehouseServices.createWarehouse(businessOwnerId, warehouse);
//
//        if (warehouseAdded == null)
//            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + warehouse, null);
//
//        return new ResponseEntity<>(warehouseAdded, HttpStatus.CREATED);
//    }
//
//    @PostMapping(path = "/addShop", consumes = "application/json", produces = "application/json")
//    public ResponseEntity<?> addShop(@RequestBody @Valid Shop shop, @RequestParam Long warehouseId){
//
//        preAuthorizeBusinessOwner();
//
//        Warehouse warehouseById = warehouseServices.warehouseById(warehouseId);
//
//        if (warehouseById == null)throw new InventoryAPIOperationException
//                ("Data not saved", "Could not find a warehouse with the id: " + warehouseId, null);
//
//        Warehouse warehouseFound = genericService.warehouseByAuthUserId()
//                .stream()
//                .filter(warehouse -> warehouse.equals(warehouseById))
//                .collect(toSingleton());
//
//        if (warehouseFound == null)throw new InventoryAPIOperationException
//                ("Data not saved", "Could not find a warehouse with the id: " + warehouseId + " in your list of warehouse", null);
//
//        Shop shopAdded = shopServices.createShop(warehouseFound, shop);
//
//        if (shopAdded == null)
//            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + shop, null);
//
//        return new ResponseEntity<>(shopAdded, HttpStatus.CREATED);
//    }
//
//    @PostMapping(path = "/addSeller", consumes = "application/json", produces = "application/json")
//    public ResponseEntity<?> addSeller(@RequestParam Long shopId, @RequestBody @Valid Seller seller){
//
//        preAuthorizeBusinessOwner();
//
//        Shop shopById = shopServices.findShopById(shopId);
//
//        if (shopById == null)throw new InventoryAPIOperationException
//                ("Data not saved", "Could not find a shop with the id: " + shopId, null);
//
//        Shop shopFound = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(shopServices::fetchAllShopInWarehouse)
//                .flatMap(List::stream)
//                .filter(shop -> shop.equals(shopById))
//                .collect(toSingleton());
//
//        if (shopFound == null)throw new InventoryAPIOperationException
//                ("Data not saved", "Could not find a shop with the id: " + shopId + " in your list of shops", null);
//
//        //Fix adding sellers twice
//        Seller sellerCreated = sellerServices.createSeller(seller);
//
//        Shop shop = shopServices.addSellerToShop(shopFound, sellerCreated);
//
//        if (shop == null)
//            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + seller + " to" +
//                    " shop with id: " + shopId, null);
//
//        return new ResponseEntity<>(sellerCreated, HttpStatus.OK);
//    }
//
//    @GetMapping(path = "/warehouses")
//    public ResponseEntity<?> fetchAllWarehouses(){
//
//        preAuthorizeBusinessOwner();
//
//        return ResponseEntity.ok(genericService.warehouseByAuthUserId());
//    }
//
//    @GetMapping(path = "/shops")
//    public ResponseEntity<?> fetchAllShops(){
//
//        preAuthorizeBusinessOwner();
//
//        List<Shop> shopList = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(shopServices::fetchAllShopInWarehouse)
//                .flatMap(List::parallelStream)
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(shopList);
//    }
//
//    @GetMapping(path = "/sellers")
//    public ResponseEntity<?> fetchAllSellers(){
//
//        preAuthorizeBusinessOwner();
//
//        List<Seller> sellerList = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(shopServices::fetchAllShopInWarehouse)
//                .flatMap(List::parallelStream)
//                .map(sellerServices::fetchShopSellersByShop)
//                .flatMap(List::parallelStream)
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(sellerList);
//    }
//
//    @GetMapping(path = "/stock")
//    public ResponseEntity<?> fetchAllStock(){
//
//        preAuthorizeBusinessOwner();
//
//        List<Stock> stockList = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(stockServices::allStockByWarehouseId)
//                .flatMap(List::parallelStream)
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(stockList);
//    }
//
//    @GetMapping(path = "/income")
//    public ResponseEntity<?> fetchAllIncome(){
//
//        preAuthorizeBusinessOwner();
//
//        Set<Income> incomeList = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(shopServices::fetchAllShopInWarehouse)
//                .flatMap(List::parallelStream)
//                .map(shop -> incomeServices.fetchAllIncomeInShop(shop.getShopId()))
//                .flatMap(List::parallelStream)
//                .collect(Collectors.toSet());
//        incomeList.addAll(incomeServices.fetchIncomeCreatedBy(AuthenticatedUserDetails.getUserFullName()));
//
//        return ResponseEntity.ok(incomeList);
//    }
//
//    @GetMapping(path = "/expenses")
//    public ResponseEntity<?> fetchAllExpenses(){
//
//        preAuthorizeBusinessOwner();
//
//        Set<Expense> expenseList = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(shopServices::fetchAllShopInWarehouse)
//                .flatMap(List::parallelStream)
//                .map(shop -> expenseServices.fetchAllExpensesInShop(shop.getShopId()))
//                .flatMap(List::parallelStream)
//                .collect(Collectors.toSet());
//        expenseList.addAll(expenseServices.fetchExpensesCreatedBy(AuthenticatedUserDetails.getUserFullName()));
//
//        return ResponseEntity.ok(expenseList);
//    }
//
//    @GetMapping(path = "/return/sales")
//    public ResponseEntity<?> fetchAllReturnSales(){
//
//        preAuthorizeBusinessOwner();
//
//        Set<ReturnedStock> returnedStocks = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(shopServices::fetchAllShopInWarehouse)
//                .flatMap(List::parallelStream)
//                .map(shop -> returnedStockServices.fetchAllStockReturnedToShop(shop.getShopId()))
//                .flatMap(List::parallelStream)
//                .collect(Collectors.toSet());
//        returnedStocks.addAll(returnedStockServices.fetchAllStockReturnedTo(AuthenticatedUserDetails.getUserFullName()));
//
//        return ResponseEntity.ok(returnedStocks);
//    }
//
//    @GetMapping(path = "/invoices")
//    public ResponseEntity<?> fetchAllInvoices(){
//
//        preAuthorizeBusinessOwner();
//
//        Set<Invoice> invoiceList = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(shopServices::fetchAllShopInWarehouse)
//                .flatMap(List::parallelStream)
//                .map(sellerServices::fetchShopSellersByShop)
//                .flatMap(List::parallelStream)
//                .map(invoiceServices::getInvoicesBySeller)
//                .flatMap(List::parallelStream)
//                .collect(Collectors.toSet());
//        invoiceList.addAll(invoiceServices.fetchAllInvoicesCreatedBy(AuthenticatedUserDetails.getUserFullName()));
//
//        return ResponseEntity.ok(invoiceList);
//    }
//
//    @GetMapping(path = "/stock/sold")
//    public ResponseEntity<?> fetchAllStockSold(){
//
//        preAuthorizeBusinessOwner();
//
//        Set<StockSold> stockSoldList = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(shopServices::fetchAllShopInWarehouse)
//                .flatMap(List::parallelStream)
//                .map(sellerServices::fetchShopSellersByShop)
//                .flatMap(List::parallelStream)
//                .map(invoiceServices::getInvoicesBySeller)
//                .flatMap(List::parallelStream)
//                .map(Invoice::getStockSold)
//                .flatMap(Set::parallelStream)
//                .collect(Collectors.toSet());
//
//        stockSoldList.addAll(invoiceServices
//                    .fetchAllInvoicesCreatedBy(AuthenticatedUserDetails.getUserFullName())
//                    .stream()
//                    .map(Invoice::getStockSold)
//                    .flatMap(Set::parallelStream)
//                    .collect(Collectors.toList()));
//
//        return ResponseEntity.ok(stockSoldList);
//    }
//
//    @GetMapping(path = "/stock/unapproved")
//    public ResponseEntity<?> fetchAllUnapprovedStock(){
//
//        preAuthorizeBusinessOwner();
//
//        Set<Stock> unApprovedStockList = genericService.warehouseByAuthUserId()
//                .parallelStream()
//                .map(Warehouse::getWarehouseId)
//                .map(stockServices::unApprovedStock)
//                .flatMap(List::parallelStream)
//                .collect(Collectors.toSet());
//
//        unApprovedStockList.addAll(stockServices.unApprovedStockByCreator(AuthenticatedUserDetails.getUserFullName()));
//
//        return ResponseEntity.ok(unApprovedStockList);
//    }
//
//    @GetMapping(path = "/income/unapproved")
//    public ResponseEntity<?> fetchAllUnapprovedIncome(){
//
//        preAuthorizeBusinessOwner();
//
//        List<Income> unApprovedIncomeList = shopServices.allUnApprovedIncome();
//
//        return ResponseEntity.ok(unApprovedIncomeList);
//    }
//
//    @GetMapping(path = "/expense/unapproved")
//    public ResponseEntity<?> fetchAllUnapprovedExpense(){
//
//        preAuthorizeBusinessOwner();
//
//        List<Expense> allUnApprovedExpense = shopServices.allUnApprovedExpense();
//
//        return ResponseEntity.ok(allUnApprovedExpense);
//    }
//
//    @GetMapping(path = "/return/sales/unapproved")
//    public ResponseEntity<?> fetchAllUnapprovedReturns(){
//
//        preAuthorizeBusinessOwner();
//
//        List<ReturnedStock> returnedStockList = shopServices.allUnApprovedReturnSales();
//
//        return ResponseEntity.ok(returnedStockList);
//    }
//
//    @GetMapping(path = "/debtors")
//    public ResponseEntity<?> fetchAllDebtors(){
//
//        preAuthorizeBusinessOwner();
//
//        Set<Customer> debtorsList = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(shopServices::fetchAllShopInWarehouse)
//                .flatMap(List::parallelStream)
//                .map(sellerServices::fetchShopSellersByShop)
//                .flatMap(List::parallelStream)
//                .map(invoiceServices::getInvoicesBySeller)
//                .flatMap(List::parallelStream)
//                .filter(invoices -> is(invoices.getDebt()).isPositive())
//                .map(Invoice::getCustomerId)
//                .collect(Collectors.toSet());
//
//        debtorsList.addAll(invoiceServices
//                .fetchAllInvoicesCreatedBy(AuthenticatedUserDetails.getUserFullName())
//                .stream()
//                .filter(invoice -> is(invoice.getDebt()).isPositive())
//                .map(Invoice::getCustomerId)
//                .collect(Collectors.toSet())
//        );
//
//        return ResponseEntity.ok(debtorsList);
//    }
//
//    @GetMapping(path = "/customer/debt")
//    public ResponseEntity<?> fetchCustomerAndDebt(@RequestParam Long customerId){
//
//        preAuthorizeBusinessOwner();
//
//        BigDecimal debtByCustomer = invoiceServices
//                .fetchInvoicesByCustomer(customerService.fetchCustomerById(customerId))
//                .stream()
//                .map(Invoice::getDebt)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        return ResponseEntity.ok(debtByCustomer);
//    }
//
//    @PutMapping(path = "/approve/stock")
//    public ResponseEntity<?> approveStock(@RequestParam Long stockId){
//
//        preAuthorizeBusinessOwner();
//
//        Stock approvedStock = stockServices.approveStock(stockId);
//
//        if (null == approvedStock) throw new InventoryAPIOperationException
//                ("Stock not approved", "Stock not approved", null);
//
//        return ResponseEntity.ok(approvedStock);
//    }
//
//    @PutMapping(path = "/approve/stockList")
//    public ResponseEntity<?> approveStockList(@RequestParam List<Long> stockIds){
//
//        preAuthorizeBusinessOwner();
//
//        List<Stock> stockList = stockServices.approveStockList(stockIds);
//
//        if (null == stockList || stockList.isEmpty()) throw new InventoryAPIOperationException
//                ("stock list not approved", "stock list not approved", null);
//
//        return ResponseEntity.ok(stockList);
//    }
//
//    @PutMapping(path = "/approve/income")
//    public ResponseEntity<?> approveIncome(@RequestParam Long incomeId){
//
//        preAuthorizeBusinessOwner();
//
//        Income approvedIncome = shopServices.approveIncome(incomeId);
//
//        if (null == approvedIncome) throw new InventoryAPIOperationException
//                ("Income not approved", "Income not approved", null);
//
//        return ResponseEntity.ok(approvedIncome);
//    }
//
//    @PutMapping(path = "/approve/return/sale")
//    public ResponseEntity<?> approveReturnSale(@RequestParam Long returnSaleId){
//
//        preAuthorizeBusinessOwner();
//
//        ReturnedStock returnedStock = shopServices.approveReturnSales(returnSaleId);
//
//        if (null == returnedStock) throw new InventoryAPIOperationException
//                ("Returned stock not approved", "Returned stock not approved", null);
//
//        return ResponseEntity.ok(returnedStock);
//    }
//
//    @DeleteMapping(path = "/delete/stock")
//    public ResponseEntity deleteStock(@RequestParam Long stockId){
//
//        preAuthorizeBusinessOwner();
//
//        Stock stockRetrieved = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(stockServices::allStockByWarehouseId)
//                .flatMap(List::stream)
//                .filter(stock -> stock.getStockId().equals(stockId))
//                .collect(toSingleton());
//
//        if (null == stockRetrieved) throw new InventoryAPIOperationException
//                ("Stock to delete not found", "Stock with id " + stockId + " was not found in any of your warehouse", null);
//
//        Stock deletedStock = stockServices.deleteEntity(stockId);
//
//        return ResponseEntity.ok(deletedStock);
//    }
//
//    @DeleteMapping(path = "/delete/stockList")
//    public ResponseEntity deleteStockList(@RequestParam List<Long> stockIds){
//
//        preAuthorizeBusinessOwner();
//
//        List<Stock> stocksToDeleteFromWarehouse = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(stockServices::allStockByWarehouseId)
//                .flatMap(List::parallelStream)
//                .filter(stock -> stockIds.contains(stock.getStockId()))
//                .collect(Collectors.toList());
//
//        if (stocksToDeleteFromWarehouse.isEmpty()) throw new InventoryAPIOperationException
//                ("Stock list to delete not found", "Stock List about to deleted were not found in any of your warehouse", null);
//
//        List<Stock> deletedStockList = stockServices.deleteStockList(stocksToDeleteFromWarehouse);
//
//        return ResponseEntity.ok(deletedStockList);
//    }
//
//    @DeleteMapping(path = "/delete/seller")
//    public ResponseEntity<?> deleteSeller(@RequestParam Long sellerId){
//
//        preAuthorizeBusinessOwner();
//
//        Seller sellerToDelete = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(shopServices::fetchAllShopInWarehouse)
//                .flatMap(List::parallelStream)
//                .map(sellerServices::fetchShopSellersByShop)
//                .flatMap(List::parallelStream)
//                .filter(seller -> seller.getSellerId().equals(sellerId))
//                .collect(toSingleton());
//
//        if (null == sellerToDelete) throw new InventoryAPIOperationException
//                ("Seller to delete not found", "Seller with id " + sellerId + " was not found in any of your shops", null);
//
//        Seller deletedSeller = sellerServices.deleteSeller(sellerId);
//
//        return ResponseEntity.ok(deletedSeller);
//    }
//
//    @DeleteMapping(path = "/delete/sellerList")
//    public ResponseEntity<?> deleteSellerList(@RequestParam List<Long> sellerIds){
//
//        preAuthorizeBusinessOwner();
//
//        List<Seller> sellersToDeleteFromShop = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(shopServices::fetchAllShopInWarehouse)
//                .flatMap(List::parallelStream)
//                .map(sellerServices::fetchShopSellersByShop)
//                .flatMap(List::parallelStream)
//                .filter(seller -> sellerIds.contains(seller.getSellerId()))
//                .collect(Collectors.toList());
//
//        if (sellersToDeleteFromShop.isEmpty()) throw new InventoryAPIOperationException
//                ("Seller List to delete not found", "Sellers with the ids provided were not found in any of your shops", null);
//
//        List<Seller> deletedSellerList = sellerServices.deleteSellerList(sellersToDeleteFromShop);
//
//        return ResponseEntity.ok(deletedSellerList);
//    }
//
//    @DeleteMapping(path = "/delete/warehouse")
//    public ResponseEntity<?> deleteWarehouse(@RequestParam Long warehouseId){
//
//        preAuthorizeBusinessOwner();
//
//        Warehouse warehouseToDelete = genericService.warehouseByAuthUserId()
//                .stream()
//                .filter(warehouse -> warehouse.getWarehouseId().equals(warehouseId))
//                .collect(toSingleton());
//
//        if (null == warehouseToDelete) throw new InventoryAPIOperationException
//                ("Warehouse to delete not found", "Warehouse with id " + warehouseId + " was not found in your account", null);
//
//        Warehouse deletedWarehouse = warehouseServices.deleteWarehouse(warehouseToDelete);
//
//        return ResponseEntity.ok(deletedWarehouse);
//    }
//
//    @DeleteMapping(path = "/delete/income")
//    public ResponseEntity<?> deleteIncome(@RequestParam Long incomeId){
//
//        preAuthorizeBusinessOwner();
//
//        List<Income> incomeList = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(shopServices::fetchAllShopInWarehouse)
//                .flatMap(List::parallelStream)
//                .map(shop -> incomeServices.fetchAllIncomeInShop(shop.getShopId()))
//                .flatMap(List::parallelStream)
//                .collect(Collectors.toList());
//        incomeList.addAll(incomeServices.fetchIncomeCreatedBy(AuthenticatedUserDetails.getUserFullName()));
//
//        Income incomeToDelete = incomeList
//                .stream()
//                .filter(income -> income.getIncomeId().equals(incomeId))
//                .collect(toSingleton());
//
//        if (null == incomeToDelete) throw new InventoryAPIOperationException
//                ("income to delete not found", "Income with id " + incomeId + " was not found in your account, or shops", null);
//
//        Income incomeDeleted = incomeServices.deleteEntity(incomeId);
//
//        return ResponseEntity.ok(incomeDeleted);
//    }
//
//    @DeleteMapping(path = "/delete/expense")
//    public ResponseEntity<?> deleteExpense(@RequestParam Long expenseId){
//
//        preAuthorizeBusinessOwner();
//
//        List<Expense> expenseList = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(shopServices::fetchAllShopInWarehouse)
//                .flatMap(List::parallelStream)
//                .map(shop -> expenseServices.fetchAllExpensesInShop(shop.getShopId()))
//                .flatMap(List::parallelStream)
//                .collect(Collectors.toList());
//        expenseList.addAll(expenseServices.fetchExpensesCreatedBy(AuthenticatedUserDetails.getUserFullName()));
//
//        Expense expenseToDelete = expenseList
//                .stream()
//                .filter(expense -> expense.getExpenseId().equals(expenseId))
//                .collect(toSingleton());
//
//        if (null == expenseToDelete) throw new InventoryAPIOperationException
//                ("expense to delete not found", "Expense with id " + expenseId + " was not found in your account, or shops", null);
//
//        Expense deletedEntity = expenseServices.deleteEntity(expenseId);
//
//        return ResponseEntity.ok(deletedEntity);
//    }
//
//    @DeleteMapping(path = "/delete/shop")
//    public ResponseEntity<?> deleteShop(@RequestParam Long shopId){
//
//        preAuthorizeBusinessOwner();
//
//        Shop shopToDelete = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(shopServices::fetchAllShopInWarehouse)
//                .flatMap(List::stream)
//                .filter(shop -> shop.getShopId().equals(shopId))
//                .collect(toSingleton());
//
//        if (null == shopToDelete) throw new InventoryAPIOperationException
//                ("shop to delete not found", "Shop with id " + shopId + " was not found in your account, or shops", null);
//
//        Shop deletedShop = shopServices.deleteEntity(shopId);
//
//        return ResponseEntity.ok(deletedShop);
//    }
//
//    @DeleteMapping(path = "/delete/stockCategory")
//    public ResponseEntity<?> deleteStockCategory(@RequestParam Long stockCategoryId){
//
//        preAuthorizeBusinessOwner();
//
//        StockCategory stockCategoryDeleted = stockServices.deleteStockCategory(stockCategoryId);
//
//        return ResponseEntity.ok(stockCategoryDeleted);
//    }
//
//    @DeleteMapping(path = "/delete/returnedSales")
//    public ResponseEntity<?> deleteReturnSale(@RequestParam Long returnedStockId){
//
//        preAuthorizeBusinessOwner();
//
//        List<ReturnedStock> returnedStocks = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(shopServices::fetchAllShopInWarehouse)
//                .flatMap(List::parallelStream)
//                .map(shop -> returnedStockServices.fetchAllStockReturnedToShop(shop.getShopId()))
//                .flatMap(List::parallelStream)
//                .collect(Collectors.toList());
//        returnedStocks.addAll(returnedStockServices.fetchAllStockReturnedTo(AuthenticatedUserDetails.getUserFullName()));
//
//        ReturnedStock returnedStockToDelete = returnedStocks.stream()
//                .filter(returnedStock -> returnedStock.getReturnedStockId().equals(returnedStockId))
//                .collect(toSingleton());
//
//        if (null == returnedStockToDelete) throw new InventoryAPIOperationException
//                ("Returned stock to delete not found",
//                        "Returned stock with id " + returnedStockId + " was not found in your account, or shops", null);
//
//        ReturnedStock deletedReturnedStock = returnedStockServices.deleteReturnedStock(returnedStockId);
//
//        return ResponseEntity.ok(deletedReturnedStock);
//    }
//
//    @DeleteMapping(path = "/delete/invoice")
//    public ResponseEntity<?> deleteInvoice(@RequestParam Long invoiceId){
//
//        preAuthorizeBusinessOwner();
//
//        List<Invoice> invoiceList = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(shopServices::fetchAllShopInWarehouse)
//                .flatMap(List::parallelStream)
//                .map(sellerServices::fetchShopSellersByShop)
//                .flatMap(List::parallelStream)
//                .map(invoiceServices::getInvoicesBySeller)
//                .flatMap(List::parallelStream)
//                .collect(Collectors.toList());
//        invoiceList.addAll(invoiceServices.fetchAllInvoicesCreatedBy(AuthenticatedUserDetails.getUserFullName()));
//
//        Invoice invoiceToDelete = invoiceList
//                .stream()
//                .filter(invoice -> invoice.getInvoiceId().equals(invoiceId))
//                .collect(toSingleton());
//
//        if (null == invoiceToDelete) throw new InventoryAPIOperationException
//                ("invoice to delete not found", "Invoice with id " + invoiceId + " was not found in your account, or shops", null);
//
//        Invoice deletedInvoice = invoiceServices.deleteEntity(invoiceId);
//
//        return ResponseEntity.ok(deletedInvoice);
//    }
//
//    @DeleteMapping(path = "/delete/supplier")
//    public ResponseEntity<?> deleteSupplier(@RequestParam Long supplierId){
//
//        preAuthorizeBusinessOwner();
//
//        Supplier supplierToDelete = stockServices.deleteSupplier(supplierId);
//
//        return ResponseEntity.ok(supplierToDelete);
//    }
//
//    @DeleteMapping(path = "/delete/customer")
//    public ResponseEntity<?> deleteCustomer(@RequestParam Long customerId){
//
//        preAuthorizeBusinessOwner();
//
//        List<Customer> customersList = genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(shopServices::fetchAllShopInWarehouse)
//                .flatMap(List::parallelStream)
//                .map(Shop::getShopId)
//                .map(customerService::fetchCustomersByShop)
//                .flatMap(List::parallelStream)
//                .collect(Collectors.toList());
//        customersList.addAll(customerService.fetchAllCustomersByCreator(AuthenticatedUserDetails.getUserFullName()));
//
//        Customer customerToDelete = customersList
//                .stream()
//                .filter(customer -> customer.getCustomerId().equals(customerId))
//                .collect(toSingleton());
//
//        if (null == customerToDelete) throw new InventoryAPIOperationException
//                ("Customer not found", "Customer with id " + customerId + " was not found in any of your shops", null);
//
//        Customer deletedCustomer = customerService.deleteCustomerById(customerId);
//
//        return ResponseEntity.ok(deletedCustomer);
//    }
//
//    //Method used to confirm that logged in user is of type business owner
//    private void preAuthorizeBusinessOwner(){
//
//        if (ACCOUNT_TYPE.SELLER.equals(AuthenticatedUserDetails.getAccount_type())) throw new InventoryAPIOperationException
//                ("Operation not allowed", "Logged in user cannot perform this operation", null);
//
//    }
//}
