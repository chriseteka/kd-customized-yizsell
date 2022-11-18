package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Services.WarehouseServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Entities.ListWrapper.prepareResponse;

/**
 * @author Chris_Eteka
 * @since 12/31/2019
 * @email chriseteka@gmail.com
 */
@RestController
@RequestMapping("/warehouse")
public class WarehouseController {

    private final WarehouseServices warehouseServices;

    @Autowired
    public WarehouseController(WarehouseServices warehouseServices) {
        this.warehouseServices = warehouseServices;
    }



    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createWarehouse(@RequestParam Long businessOwnerId,
                                        @RequestBody @Valid Warehouse warehouse){

        Warehouse warehouseCreated = warehouseServices.createWarehouse(businessOwnerId, warehouse);

        if (null == warehouseCreated) throw new InventoryAPIOperationException("Warehouse not created",
                "Warehouse was not created successfully, review your inputs and try again", null);

        return ResponseEntity.ok(warehouseCreated);
    }

    @GetMapping(path = "/byId")
    public ResponseEntity<?> fetchWarehouseById(@RequestParam Long warehouseId){

        Warehouse warehouse = warehouseServices.warehouseById(warehouseId);

        if (null == warehouse) throw new InventoryAPIResourceNotFoundException("Warehouse not found",
                "Warehouse with id: " + warehouseId + " was not found, review your inputs and try again", null);

        return ResponseEntity.ok(warehouse);
    }

    @PutMapping(path = "/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateWarehouse(@RequestParam Long warehouseId,
                                        @RequestBody @Valid Warehouse warehouse){

        Warehouse updatedWarehouse = warehouseServices.updateWarehouse(warehouseId, warehouse);

        if (null == updatedWarehouse)throw new InventoryAPIOperationException("Warehouse not updated",
                "Warehouse not updated successfully, review your inputs and try again", null);

        return ResponseEntity.ok(updatedWarehouse);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<?> fetchAllWarehouses(@RequestParam int page, @RequestParam int size){

        List<Warehouse> warehouseList = warehouseServices.fetchAllWarehouse()
                .stream()
                .sorted(Comparator.comparing(Warehouse::getCreatedDate)
                    .thenComparing(Warehouse::getCreatedTime).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(prepareResponse(warehouseList, page, size));
    }

    @GetMapping(path = "/byWarehouseAttendant")
    public ResponseEntity<?> fetchWarehouseBySeller(@RequestParam String warehouseAttendant){

        Warehouse warehouse = warehouseServices.fetchWarehouseByWarehouseAttendant(warehouseAttendant);

        if(null == warehouse) throw new InventoryAPIResourceNotFoundException("Warehouse not found",
                "Warehouse was not retrieved with the warehouseAttendant name/email: " + warehouseAttendant, null);

        return ResponseEntity.ok(warehouse);
    }

    @DeleteMapping(path = "/delete/byId")
    public ResponseEntity<?> deleteWarehouseByWarehouseId(@RequestParam Long[] warehouseId){

        return ResponseEntity.ok(warehouseServices.deleteWarehouses(warehouseId));
    }
}
