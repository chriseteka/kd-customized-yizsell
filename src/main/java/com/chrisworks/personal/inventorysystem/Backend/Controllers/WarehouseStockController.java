package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.APPLICATION_EVENTS;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.WarehouseStocks;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Services.WarehouseStockServices;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.chrisworks.personal.inventorysystem.Backend.Utility.Events.SellerTriggeredEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.futureDate;

/**
 * @author Chris_Eteka
 * @since 12/31/2019
 * @email chriseteka@gmail.com
 */
@RestController
@RequestMapping("/warehouseStock")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class WarehouseStockController {

    private final WarehouseStockServices warehouseStockServices;

    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public WarehouseStockController(WarehouseStockServices warehouseStockServices, ApplicationEventPublisher eventPublisher) {
        this.warehouseStockServices = warehouseStockServices;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createStockInWarehouse(@RequestParam Long warehouseId,
                                               @RequestBody @Valid WarehouseStocks stock){

        WarehouseStocks newStockInWarehouse = warehouseStockServices.createStockInWarehouse(warehouseId, stock);

        if (null == newStockInWarehouse) throw new InventoryAPIOperationException("Stock not created",
                "Stock not created successfully in warehouse, review your inputs and try again", null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    "A new stock with name: " + stock.getStockName() + " has been added to your warehouse.",
                    APPLICATION_EVENTS.WAREHOUSE_STOCK_UP_EVENT));
        }

        return ResponseEntity.ok(newStockInWarehouse);
    }

    @PostMapping(path = "/create/list", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createStockListInWarehouse(@RequestParam Long warehouseId,
                                                    @RequestBody @Valid List<WarehouseStocks> stockList){

        if (stockList.isEmpty()) throw new InventoryAPIOperationException("Empty list of stocks",
                "You are trying to save an empty list of stock, this is not allowed", null);

        List<WarehouseStocks> newStockListInWarehouse = warehouseStockServices
                .createStockListInWarehouse(warehouseId, stockList);

        if (null == newStockListInWarehouse || newStockListInWarehouse.isEmpty())
            throw new InventoryAPIOperationException("Stock not created",
                "Stock not created successfully in warehouse, review your inputs and try again", null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    "New stock list with size: " + stockList.size() + " were uploaded to your warehouse.",
                    APPLICATION_EVENTS.WAREHOUSE_STOCK_UP_EVENT));
        }

        return ResponseEntity.ok(newStockListInWarehouse);
    }

    @GetMapping(path = "/all/byWarehouse")
    public ResponseEntity<?> fetchAllByWarehouseId(@RequestParam Long warehouseId, @RequestParam int page,
                                                   @RequestParam int size){

        if (page == 0 || size == 0) return ResponseEntity.ok(warehouseStockServices.allStockByWarehouseId(warehouseId));

        List<WarehouseStocks> warehouseStocksList = warehouseStockServices.allStockByWarehouseId(warehouseId)
                .stream()
                .sorted(Comparator.comparing(WarehouseStocks::getCreatedDate).reversed())
                .skip((size * (page - 1)))
                .limit(size)
                .collect(Collectors.toList());

        return ResponseEntity.ok(warehouseStocksList);
    }

    @GetMapping(path = "/all/soonToFinish/byWarehouse")
    public ResponseEntity<?> fetchAllSoonToFinishInWarehouse(@RequestParam Long warehouseId,
                                                        @RequestParam int limit){

        return ResponseEntity.ok(warehouseStockServices.allSoonToFinishStock(warehouseId, limit));
    }

    @GetMapping(path = "/all/soonToExpire/byWarehouse")
    public ResponseEntity<?> fetchAllSoonToExpireInWarehouse(@RequestParam Long warehouseId){

        return ResponseEntity.ok(warehouseStockServices
                .allSoonToExpireStock(warehouseId, futureDate(60)));
    }

    @GetMapping(path = "/all/approved/byWarehouse")
    public ResponseEntity<?> fetchAllApprovedStockInWarehouse(@RequestParam Long warehouseId){

        return ResponseEntity.ok(warehouseStockServices.allApprovedStock(warehouseId));
    }

    @GetMapping(path = "/all/unApproved/byWarehouse")
    public ResponseEntity<?> fetchAllUnApprovedStockInWarehouse(@RequestParam Long warehouseId){

        return ResponseEntity.ok(warehouseStockServices.allUnApprovedStock(warehouseId));
    }

    @GetMapping(path = "/all/unApproved/byCreator")
    public ResponseEntity<?> fetchAllUnApprovedStockCreatedBy(@RequestParam String createdBy){

        return ResponseEntity.ok(warehouseStockServices.allUnApprovedStockByCreator(createdBy));
    }

    @PutMapping(path = "/approve/byStockId")
    public ResponseEntity<?> approveShopStockByStockId(@RequestParam Long stockId){

        WarehouseStocks approvedStock = warehouseStockServices.approveStock(stockId);

        if (null == approvedStock) throw new InventoryAPIOperationException("Warehouse stock not approved",
                "Stock not approved successfully, review your inputs and try again", null);

        return ResponseEntity.ok(approvedStock);
    }

    @PutMapping(path = "/approve/byStockIdList")
    public ResponseEntity<?> approveShopStockByStockIdList(@RequestParam List<Long> stockIdList){

        return ResponseEntity.ok(warehouseStockServices.approveStockList(stockIdList));
    }

    @DeleteMapping(path = "/delete/byStockId")
    public ResponseEntity<?> deleteWarehouseStockByStockId(@RequestParam Long stockId){

        WarehouseStocks deletedStock = warehouseStockServices.deleteStock(stockId);

        if (null == deletedStock) throw new InventoryAPIOperationException("Warehouse stock not deleted",
                "Stock with id: " + stockId + " was not deleted successfully, review your inputs and try again", null);

        return ResponseEntity.ok(deletedStock);
    }

    @PutMapping(path = "/restock", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> restockExistingShopWarehouseStock(@RequestParam Long warehouseId,
                                                      @RequestParam Long stockId,
                                                      @RequestBody @Valid WarehouseStocks stock){

        WarehouseStocks reStockToWarehouse = warehouseStockServices.reStockToWarehouse(warehouseId, stockId, stock);

        if (null == reStockToWarehouse) throw new InventoryAPIOperationException("Restock failed",
                "Restock failed, review your inputs and try again", null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    "Restock just occurred on a stock with name: " + stock.getStockName() +
                            " has been added to previously existing ones in your warehouse.",
                    APPLICATION_EVENTS.RESTOCK_EVENT));
        }

        return ResponseEntity.ok(reStockToWarehouse);
    }

    @PutMapping(path = "/changeSellingPrice/byStockId")
    public ResponseEntity<?> changeStockSellingPriceByStockId(@RequestParam Long stockId,
                                                              @RequestParam BigDecimal newSellingPrice){

        WarehouseStocks warehouseStock = warehouseStockServices.changeStockSellingPriceByStockId(stockId, newSellingPrice);

        if (warehouseStock == null)
            throw new InventoryAPIOperationException("Data not updated",
                    "Could not change selling price for stock with value: " + newSellingPrice.toString(), null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    "Selling price of stock in your warehouse with name: " + warehouseStock.getStockName() +
                            " has been changed to: " + newSellingPrice,
                    APPLICATION_EVENTS.SELLING_PRICE_CHANGED_EVENT));
        }

        return ResponseEntity.ok(warehouseStock);
    }

    @PutMapping(path = "/changeSellingPrice/byStockName")
    public ResponseEntity<?> changeStockSellingPriceByName(@RequestParam Long warehouseId,
                                                           @RequestParam String stockName,
                                                           @RequestParam BigDecimal newSellingPrice){

        WarehouseStocks warehouseStock = warehouseStockServices
                .changeStockSellingPriceByWarehouseIdAndStockName(warehouseId, stockName, newSellingPrice);

        if (warehouseStock == null)
            throw new InventoryAPIOperationException("Data not updated",
                    "Could not change selling price for stock with value: " + newSellingPrice.toString(), null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    "Selling price of stock in your warehouse with name: " + stockName +
                            " has been changed to: " + newSellingPrice,
                    APPLICATION_EVENTS.SELLING_PRICE_CHANGED_EVENT));
        }

        return ResponseEntity.ok(warehouseStock);
    }
}