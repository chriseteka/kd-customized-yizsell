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
//}
