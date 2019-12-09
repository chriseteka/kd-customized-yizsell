package com.chrisworks.personal.inventorysystem.Backend.Controllers.BusinessOwnerController;

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

    @Autowired
    public BusinessOwnerController(BusinessOwnerServices businessOwnerServices, GenericService genericService,
                                   ShopServices shopServices, SellerServices sellerServices,
                                   WarehouseServices warehouseServices, StockServices stockServices,
                                   IncomeServices incomeServices, ExpenseServices expenseServices) {
        this.businessOwnerServices = businessOwnerServices;
        this.genericService = genericService;
        this.shopServices = shopServices;
        this.sellerServices = sellerServices;
        this.warehouseServices = warehouseServices;
        this.stockServices = stockServices;
        this.incomeServices = incomeServices;
        this.expenseServices = expenseServices;
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
        expenseList.addAll(expenseServices.fetchExpensesCreatedBy(AuthenticatedUserDetails.getUserFullName()));

        return ResponseEntity.ok(expenseList);
    }
}
