package com.chrisworks.personal.inventorysystem.Backend.Controllers.BusinessOwnerController;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import com.chrisworks.personal.inventorysystem.Backend.Services.BusinessOwnerServices;
import com.chrisworks.personal.inventorysystem.Backend.Services.GenericService;
import com.chrisworks.personal.inventorysystem.Backend.Services.SellerServices;
import com.chrisworks.personal.inventorysystem.Backend.Services.ShopServices;
import com.chrisworks.personal.inventorysystem.Backend.Services.WarehouseServices;
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
@RequestMapping("/BO")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BusinessOwnerController {

    private BusinessOwnerServices businessOwnerServices;

    private GenericService genericService;

    private ShopServices shopServices;

    private SellerServices sellerServices;

    private WarehouseServices warehouseServices;

    @Autowired
    public BusinessOwnerController(BusinessOwnerServices businessOwnerServices, GenericService genericService,
                                   ShopServices shopServices, SellerServices sellerServices,
                                   WarehouseServices warehouseServices) {
        this.businessOwnerServices = businessOwnerServices;
        this.genericService = genericService;
        this.shopServices = shopServices;
        this.sellerServices = sellerServices;
        this.warehouseServices = warehouseServices;
    }

    @PostMapping(path = "/createAccount", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createAccount(@RequestBody @Valid BusinessOwner businessOwner){

        return ResponseEntity.ok(businessOwnerServices.createAccount(businessOwner));
    }

    @GetMapping(path = "/id")
    public ResponseEntity<?> fetchBusinessOwner(@RequestParam Long id){

        BusinessOwner businessOwner = businessOwnerServices.fetchBusinessOwner(id);

        return businessOwner != null ? ResponseEntity.ok(businessOwner) : ResponseEntity.notFound().build();
    }

    @PostMapping(path = "/updateAccount/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateProfile(@RequestBody @Valid BusinessOwner businessOwner,
                                           @PathVariable("id") Long businessOwnerId){

        return ResponseEntity.ok(businessOwnerServices.updateAccount(businessOwnerId, businessOwner));
    }

    @GetMapping(path = "/sellerShop")
    public ResponseEntity<?> shopBySeller(@RequestParam String sellerName){

        Shop shopRetrieved = genericService.shopBySellerName(sellerName);

        return shopRetrieved != null ? ResponseEntity.ok(shopRetrieved) : ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/addWarehouse", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addWarehouse(@RequestBody Warehouse warehouse, @RequestParam Long businessOwnerId){

        Warehouse warehouseAdded = warehouseServices.addWarehouse(businessOwnerId, warehouse);

        return warehouseAdded != null ? ResponseEntity.ok(warehouseAdded) : ResponseEntity.badRequest().body(null);
    }

    @PostMapping(path = "/addShop", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addShop(@RequestBody Shop shop, @RequestParam Long warehouseId){

        Shop shopAdded = shopServices.addShop(warehouseId, shop);

        return shopAdded != null ? ResponseEntity.ok(shopAdded) : ResponseEntity.badRequest().body(shop);
    }

    @PostMapping(path = "/addSellerToShop", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addSellerToShop(@RequestParam Long shopId, @RequestParam Long sellerId){

        Shop shop = shopServices.addSellerToShop(shopId, sellerServices.fetchSellerById(sellerId));

        return shop != null ? ResponseEntity.ok(shop) : ResponseEntity.badRequest().build();
    }

    @GetMapping(path = "/shops")
    public ResponseEntity<?> fetchAllShop(@RequestParam Long warehouseId){

        List<Shop> shops = shopServices.fetchAllShopInWarehouse(warehouseId);

        return shops != null ? ResponseEntity.ok(shops) : ResponseEntity.noContent().build();
    }
}
