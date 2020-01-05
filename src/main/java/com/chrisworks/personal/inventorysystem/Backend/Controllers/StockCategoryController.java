package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockCategory;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Services.StockCategoryServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Chris_Eteka
 * @since 12/31/2019
 * @email chriseteka@gmail.com
 */
@RestController
@RequestMapping("/stockCategory")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class StockCategoryController {

    private final StockCategoryServices stockCategoryServices;

    @Autowired
    public StockCategoryController(StockCategoryServices stockCategoryServices) {
        this.stockCategoryServices = stockCategoryServices;
    }

    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createStockCategory(@RequestBody @Valid StockCategory stockCategory){

        StockCategory stockCategoryCreated = stockCategoryServices.createEntity(stockCategory);

        if (null == stockCategoryCreated) throw new InventoryAPIOperationException("Stock category not created",
                "Stock Category could not be created, review your inputs and try again", null);

        return ResponseEntity.ok(stockCategoryCreated);
    }

    @PutMapping(path = "/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateStockCategory(@RequestParam Long stockCategoryId,
                                                 @RequestBody @Valid StockCategory stockCategory){

        StockCategory updatedStockCategory = stockCategoryServices.updateEntity(stockCategoryId, stockCategory);

        if (null == updatedStockCategory) throw new InventoryAPIOperationException("Stock category not updated",
                "Stock category not updated successfully, review your inputs and try again", null);

        return ResponseEntity.ok(updatedStockCategory);
    }

    @GetMapping(path = "/byId")
    public ResponseEntity<?> fetchStockCategoryById(@RequestParam Long stockCategoryId){

        StockCategory stockCategory = stockCategoryServices.getSingleEntity(stockCategoryId);

        if (null == stockCategory) throw new InventoryAPIResourceNotFoundException("Stock Category not found",
                "Stock category with id: " + stockCategoryId + " was not found in your list of stock categories", null);

        return ResponseEntity.ok(stockCategory);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<?> fetchAllStockCategoryInABusiness(@RequestParam int page, @RequestParam int size){

        if (page == 0 || size == 0) return ResponseEntity.ok(stockCategoryServices.getEntityList());

        List<StockCategory> stockCategoryList = stockCategoryServices.getEntityList()
                .stream()
                .sorted(Comparator.comparing(StockCategory::getCreatedDate).reversed())
                .skip((size * (page - 1)))
                .limit(size)
                .collect(Collectors.toList());

        return ResponseEntity.ok(stockCategoryList);
    }

    @DeleteMapping(path = "/byId")
    public ResponseEntity deleteStockCategoryById(@RequestParam Long stockCategoryId){

        StockCategory deletedStockCategory = stockCategoryServices.deleteEntity(stockCategoryId);

        if (null == deletedStockCategory) throw new InventoryAPIOperationException("Stock category not deleted",
                "Stock category with id: " + stockCategoryId + " was not deleted successfully," +
                        " review your inputs and try again", null);

        return ResponseEntity.ok(deletedStockCategory);
    }

    @GetMapping(path = "/byName")
    public ResponseEntity<?> fetchStockCategoryByName(@RequestParam String stockCategoryName){

        StockCategory stockCategory = stockCategoryServices.fetchStockCategoryByName(stockCategoryName);

        if (null == stockCategory) throw new InventoryAPIResourceNotFoundException("Stock Category not found",
                "Stock category with name: " + stockCategoryName + " was not found in your list of stock categories", null);

        return ResponseEntity.ok(stockCategory);
    }

    @GetMapping(path = "/all/byCreator")
    public ResponseEntity<?> fetchAllStockCategoryByCreator(@RequestParam String createdBy){

        return ResponseEntity.ok(stockCategoryServices.fetchAllStockCategoryByCreatedBy(createdBy));
    }
}
