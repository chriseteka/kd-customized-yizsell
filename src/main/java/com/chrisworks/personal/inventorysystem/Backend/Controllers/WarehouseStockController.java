package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.WarehouseStocks;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Services.WarehouseStockServices;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public WarehouseStockController(WarehouseStockServices warehouseStockServices) {
        this.warehouseStockServices = warehouseStockServices;
    }

    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createStockInWarehouse(@RequestParam Long warehouseId,
                                               @RequestBody @Valid WarehouseStocks stock){

        WarehouseStocks newStockInWarehouse = warehouseStockServices.createStockInWarehouse(warehouseId, stock);

        if (null == newStockInWarehouse) throw new InventoryAPIOperationException("Stock not created",
                "Stock not created successfully in warehouse, review your inputs and try again", null);

        return ResponseEntity.ok(newStockInWarehouse);
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

        return ResponseEntity.ok(reStockToWarehouse);
    }

    @PutMapping(path = "/changeSellingPrice/byStockId")
    public ResponseEntity<?> changeStockSellingPriceByStockId(@RequestParam Long stockId,
                                                              @RequestParam BigDecimal newSellingPrice){

        WarehouseStocks warehouseStock = warehouseStockServices.changeStockSellingPriceByStockId(stockId, newSellingPrice);

        if (warehouseStock == null)
            throw new InventoryAPIOperationException("Data not updated",
                    "Could not change selling price for stock with value: " + newSellingPrice.toString(), null);

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

        return ResponseEntity.ok(warehouseStock);
    }
}
