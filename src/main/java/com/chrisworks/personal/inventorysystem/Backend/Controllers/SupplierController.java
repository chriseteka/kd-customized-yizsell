package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Supplier;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Services.SupplierServices;
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
@RequestMapping("/supplier")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SupplierController {

    private final SupplierServices supplierServices;

    @Autowired
    public SupplierController(SupplierServices supplierServices) {
        this.supplierServices = supplierServices;
    }

    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createSupplier(@RequestBody @Valid Supplier supplier){

        Supplier supplierCreated = supplierServices.createEntity(supplier);

        if (null == supplierCreated) throw new InventoryAPIOperationException("Supplier not created",
                "Supplier could not be created, review your inputs and try again", null);

        return ResponseEntity.ok(supplierCreated);
    }

    @PutMapping(path = "/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateSupplier(@RequestParam Long supplierId,
                                                 @RequestBody @Valid Supplier supplier){

        Supplier updatedSupplier = supplierServices.updateEntity(supplierId, supplier);

        if (null == updatedSupplier) throw new InventoryAPIOperationException("Supplier not updated",
                "Supplier not updated successfully, review your inputs and try again", null);

        return ResponseEntity.ok(updatedSupplier);
    }

    @GetMapping(path = "/byId")
    public ResponseEntity<?> fetchSupplierById(@RequestParam Long supplierId){

        Supplier supplier = supplierServices.getSingleEntity(supplierId);

        if (null == supplier) throw new InventoryAPIResourceNotFoundException("Supplier not found",
                "Supplier with id: " + supplierId + " was not found in your list of stock categories", null);

        return ResponseEntity.ok(supplier);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<?> fetchAllSupplierInABusiness(@RequestParam int page, @RequestParam int size){

        List<Supplier> supplierList = supplierServices.getEntityList()
                .stream()
                .sorted(Comparator.comparing(Supplier::getCreatedDate).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(prepareResponse(supplierList, page, size));
    }

    @DeleteMapping(path = "/byId")
    public ResponseEntity deleteSupplierById(@RequestParam Long supplierId){

        Supplier deletedSupplier = supplierServices.deleteEntity(supplierId);

        if (null == deletedSupplier) throw new InventoryAPIOperationException("Supplier not deleted",
                "Supplier with id: " + supplierId + " was not deleted successfully," +
                        " review your inputs and try again", null);

        return ResponseEntity.ok(deletedSupplier);
    }

    @GetMapping(path = "/byName")
    public ResponseEntity<?> fetchSupplierByName(@RequestParam String supplierName){

        Supplier supplier = supplierServices.fetchSupplierByName(supplierName);

        if (null == supplier) throw new InventoryAPIResourceNotFoundException("Supplier not found",
                "Supplier with name: " + supplierName + " was not found in your list of stock categories", null);

        return ResponseEntity.ok(supplier);
    }

    @GetMapping(path = "/byPhone")
    public ResponseEntity<?> fetchSupplierByPhoneNumber(@RequestParam String supplierPhone){

        Supplier supplier = supplierServices.fetchSupplierByPhoneNumber(supplierPhone);

        if (null == supplier) throw new InventoryAPIResourceNotFoundException("Supplier not found",
                "Supplier with name: " + supplierPhone + " was not found in your list of stock categories", null);

        return ResponseEntity.ok(supplier);
    }

    @GetMapping(path = "/all/byCreator")
    public ResponseEntity<?> fetchAllSupplierByCreator(@RequestParam String createdBy){

        return ResponseEntity.ok(supplierServices.fetchSupplierByCreator(createdBy));
    }
}
