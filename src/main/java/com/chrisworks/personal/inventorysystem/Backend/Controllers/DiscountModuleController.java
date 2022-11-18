package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.DiscountModel;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Services.DiscountModelServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/discount/model")
public class DiscountModuleController {

    private final DiscountModelServices discountModelServices;

    @Autowired
    public DiscountModuleController(DiscountModelServices discountModelServices) {
        this.discountModelServices = discountModelServices;
    }

    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createDiscountModel(@RequestBody @Valid DiscountModel discountModel){

        DiscountModel newDiscountModel = discountModelServices.createEntity(discountModel);

        if (null == newDiscountModel) throw new InventoryAPIOperationException("Cannot save",
                "Cannot create a new discount model, review your inputs and try again", null);

        return new ResponseEntity<>(newDiscountModel, HttpStatus.CREATED);
    }

    @PutMapping(path = "/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateDiscountModel(@RequestParam Long discountModelId,
                                                 @RequestBody @Valid DiscountModel discountModel){

        return ResponseEntity.ok(discountModelServices.updateEntity(discountModelId, discountModel));
    }

    @GetMapping(path = "/byId", produces = "application/json")
    public ResponseEntity<?> findDiscountModelById(@RequestParam Long discountModelId){

        return ResponseEntity.ok(discountModelServices.getSingleEntity(discountModelId));
    }

    @GetMapping(path = "/all", produces = "application/json")
    public ResponseEntity<?> findAll(){

        return ResponseEntity.ok(discountModelServices.getEntityList());
    }

    @DeleteMapping(path = "/delete/byId", produces = "application/json")
    public ResponseEntity<?> deleteDiscountModelById(@RequestParam Long discountModelId){

        return ResponseEntity.ok(discountModelServices.deleteEntity(discountModelId));
    }
}
