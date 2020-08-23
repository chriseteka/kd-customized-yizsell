package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Services.BusinessOwnerServices;
import com.chrisworks.personal.inventorysystem.Backend.Services.GenericService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.validation.Valid;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;

@RestController
@RequestMapping("/BO")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BussOwnerController {

    private final BusinessOwnerServices businessOwnerServices;

    private final GenericService genericService;

    @Autowired
    public BussOwnerController(BusinessOwnerServices businessOwnerServices, GenericService genericService) {
        this.businessOwnerServices = businessOwnerServices;
        this.genericService = genericService;
    }

    @PostMapping(path = "/createAccount", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createAccount(@RequestBody @Valid BusinessOwner businessOwner, WebRequest request){

        BusinessOwner businessOwnerAccount = businessOwnerServices.createAccount(businessOwner, request);

        if (businessOwnerAccount == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not save entity: " + businessOwner, null);

        return new ResponseEntity<>(businessOwnerAccount, HttpStatus.CREATED);
    }

    @PutMapping(path = "/updateAccount/id", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateProfile(@RequestBody @Valid BusinessOwner businessOwner,
                                           @RequestParam Long businessOwnerId){

        BusinessOwner updatedAccount = businessOwnerServices.updateAccount(businessOwnerId, businessOwner);

        if (updatedAccount == null)
            throw new InventoryAPIOperationException("Data not saved", "Could not update entity: " + businessOwner, null);

        return new ResponseEntity<>(updatedAccount, HttpStatus.OK);
    }

    @GetMapping(path = "/details", produces = "application/json")
    public ResponseEntity<?> fetchBusinessDetails(){

        return ResponseEntity.ok(businessOwnerServices.fetchBusinessOwnerByAuthUser());
    }

    @PutMapping(path = "/activate/deactivate/seller")
    public ResponseEntity<?> activateSeller(@RequestParam Long sellerId){

        Seller sellerFound = genericService.sellersByAuthUserId()
                .stream()
                .filter(seller -> seller.getSellerId().equals(sellerId))
                .collect(toSingleton());

        if (null == sellerFound)
            throw new InventoryAPIOperationException("Seller not found", "Seller with id: " + sellerId +
                        " was not found in you list of inactive sellers", null);

        if (!sellerFound.getIsActive()) {

            Seller sellerActivated = businessOwnerServices.activateSeller(sellerId);

            if (null == sellerActivated) throw new InventoryAPIOperationException
                    ("Seller not activated", "Seller could not be activated, please try again", null);

            return ResponseEntity.ok(sellerActivated);
        }else{

            Seller sellerDeactivated = businessOwnerServices.deactivateSeller(sellerId);

            if (null == sellerDeactivated) throw new InventoryAPIOperationException
                    ("Seller not deactivated", "Seller could not be deactivated, please try again", null);

            return ResponseEntity.ok(sellerDeactivated);
        }
    }

    @PutMapping(path = "/updateSeller", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateSeller(@RequestParam Long sellerId, @RequestBody @Valid Seller sellerUpdates){

        Seller sellerFound = genericService.sellersByAuthUserId()
                .stream()
                .filter(seller -> seller.getSellerId().equals(sellerId))
                .collect(toSingleton());

        if (null == sellerFound)throw new InventoryAPIOperationException
                ("Seller not found", "Seller with id: " + sellerId + " was not found in you list of sellers", null);

        Seller updatedSeller = businessOwnerServices.updateSeller(sellerId, sellerUpdates);

        if (null == updatedSeller)throw new InventoryAPIOperationException
                ("Seller account not updated", "Seller account could not be updated, please try again", null);

        return ResponseEntity.ok(updatedSeller);
    }

    @PutMapping(path = "/assignSeller/toShop")
    public ResponseEntity<?> assignSellerToShop(@RequestParam Long shopId, @RequestParam Long sellerId){

        Seller seller = businessOwnerServices.assignSellerToShop(sellerId, shopId);

        if (null == seller) throw new InventoryAPIOperationException("Seller not assigned to shop",
                "Seller could not be assigned to a shop successfully, try again", null);

        return ResponseEntity.ok(seller);
    }

    @PutMapping(path = "/unassignSeller/fromShop")
    public ResponseEntity<?> unassignSellerFromShop(@RequestParam Long shopId, @RequestParam Long sellerId){

        Seller seller = businessOwnerServices.unAssignSellerFromShop(sellerId, shopId);

        if (null == seller) throw new InventoryAPIOperationException("Seller not assigned to shop",
                "Seller could not be unassigned from a shop successfully, try again", null);

        return ResponseEntity.ok(seller);
    }

    @PutMapping(path = "/assignSeller/toWarehouse")
    public ResponseEntity<?> assignSellerToWarehouse(@RequestParam Long warehouseId, @RequestParam Long sellerId){

        Seller seller = businessOwnerServices.assignSellerToWarehouse(sellerId, warehouseId);

        if (null == seller) throw new InventoryAPIOperationException("Seller not assigned to shop",
                "Seller could not be assigned to a warehouse successfully, try again", null);

        return ResponseEntity.ok(seller);
    }

    @PutMapping(path = "/unassignSeller/fromWarehouse")
    public ResponseEntity<?> unassignSellerFromWarehouse(@RequestParam Long warehouseId, @RequestParam Long sellerId){

        Seller seller = businessOwnerServices.unAssignSellerFromWarehouse(sellerId, warehouseId);

        if (null == seller) throw new InventoryAPIOperationException("Seller not assigned to shop",
                "Seller could not be unassigned from a warehouse successfully, try again", null);

        return ResponseEntity.ok(seller);
    }
}
