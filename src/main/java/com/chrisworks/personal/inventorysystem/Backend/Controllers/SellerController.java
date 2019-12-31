package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Services.SellerServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/27/2019
 * @email chriseteka@gmail.com
 */
@RestController
@RequestMapping("/SC")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SellerController {

    private SellerServices sellerServices;

    @Autowired
    public SellerController(SellerServices sellerServices) {
        this.sellerServices = sellerServices;
    }

    @PostMapping(path = "/createSeller", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createSeller(@RequestBody @Valid Seller seller){

        Seller sellerCreated = sellerServices.createSeller(seller);

        if (null == sellerCreated) throw new InventoryAPIOperationException("Seller not created",
                "Seller not created successfully, review your inputs and try again", null);

        return ResponseEntity.ok(sellerCreated);
    }

    @GetMapping(path = "/seller/id")
    public ResponseEntity<?> fetchSellerById(@RequestParam Long id){

        Seller seller = sellerServices.fetchSellerById(id);

        if(seller == null) throw new InventoryAPIResourceNotFoundException("seller not found",
                "Seller with seller id: " + id + " was not found in your list of sellers", null);

        return ResponseEntity.ok(seller);
    }

    @GetMapping(path = "/seller/name")
    public ResponseEntity<?> fetchSellerByName(@RequestParam String sellerName){

        return ResponseEntity.ok(sellerServices.fetchSellerByNameOrEmail(sellerName));
    }

    @GetMapping(path = "/all/warehouseAttendant/byWarehouse")
    public ResponseEntity<?> fetchAllWarehouseAttendantByWarehouseId(@RequestParam Long warehouseId){

        return ResponseEntity.ok(sellerServices.fetchAllWarehouseAttendantByWarehouseId(warehouseId));
    }

    @GetMapping(path = "/all/shopSellers/byShop")
    public ResponseEntity<?> fetchAllShopSellersByShopId(@RequestParam Long shopId){

        return ResponseEntity.ok(sellerServices.fetchAllShopSellersByShopId(shopId));
    }

    @PutMapping(path = "/updateSeller")
    public ResponseEntity<?> updateSellerDetails(@RequestParam Long sellerId,
                                                 @RequestBody @Valid Seller seller){

        Seller updatedSeller = sellerServices.updateSeller(sellerId, seller);

        if (null == updatedSeller) throw new InventoryAPIOperationException("Seller not updated",
                "Seller not updated successfully, review your inputs and try again", null);

        return ResponseEntity.ok(updatedSeller);
    }

    @DeleteMapping(path = "/deleteSeller")
    public ResponseEntity<?> deleteSeller(@RequestParam Long sellerId){

        Seller deletedSeller = sellerServices.deleteSeller(sellerId);

        if (null == deletedSeller) throw new InventoryAPIOperationException("Seller not deleted",
                "Seller was not deleted successfully, review your inputs and try again", null);

        return ResponseEntity.ok(deletedSeller);
    }

    @DeleteMapping(path = "/deleteSellerList")
    public ResponseEntity<?> deleteSellerList(@RequestParam List<Long> sellerIds){

        return ResponseEntity.ok(sellerServices.deleteSellerList(sellerIds));
    }

    @GetMapping(path = "/all/shopSellers")
    public ResponseEntity<?> fetchAllShopSellers() {

        return ResponseEntity.ok(sellerServices.fetchShopSellersByLoggedInUser());
    }

    @GetMapping(path = "/all/warehouseAttendants")
    public ResponseEntity<?> fetchAllWarehouseAttendants() {

        return ResponseEntity.ok(sellerServices.fetchWarehouseAttendantsByLoggedInUser());
    }

    @GetMapping(path = "/all/sellers")
    public ResponseEntity<?> fetchAllSellers() {

        return ResponseEntity.ok(sellerServices.fetchSellers());
    }
}
