package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Services.GenericService;
import com.chrisworks.personal.inventorysystem.Backend.Services.ShopServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Chris_Eteka
 * @since 12/30/2019
 * @email chriseteka@gmail.com
 */
@RestController
@RequestMapping("/shop")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ShopController {

    private final ShopServices shopServices;

    private final GenericService genericService;

    @Autowired
    public ShopController(ShopServices shopServices, GenericService genericService) {
        this.shopServices = shopServices;
        this.genericService = genericService;
    }

    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createShop(@RequestParam Long businessOwnerId,
                                        @RequestBody @Valid Shop shop){

        Shop shopCreated = shopServices.createShop(businessOwnerId, shop);

        if (null == shopCreated) throw new InventoryAPIOperationException("Shop not created",
                "Shop was not created successfully, review your inputs and try again", null);

        return ResponseEntity.ok(shopCreated);
    }

    @GetMapping(path = "/byId")
    public ResponseEntity<?> fetchShopById(@RequestParam Long shopId){

        Shop shopById = shopServices.findShopById(shopId);

        if (null == shopById) throw new InventoryAPIResourceNotFoundException("Shop not found",
                "Shop with id: " + shopId + " was not found, review your inputs and try again", null);

        return ResponseEntity.ok(shopById);
    }

    @PutMapping(path = "/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateShop(@RequestParam Long shopId,
                                        @RequestBody @Valid Shop shop){

        Shop updatedShop = shopServices.updateShop(shopId, shop);

        if (null == updatedShop)throw new InventoryAPIOperationException("Shop not updated",
                "Shop not updated successfully, review your inputs and try again", null);

        return ResponseEntity.ok(updatedShop);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<?> fetchAllShops(@RequestParam int page, @RequestParam int size){

        if (page == 0 || size == 0) return ResponseEntity.ok(shopServices.fetchAllShops());

        List<Shop> shopList = shopServices.fetchAllShops()
                .stream()
                .sorted(Comparator.comparing(Shop::getCreatedDate).reversed())
                .skip((size * (page - 1)))
                .limit(size)
                .collect(Collectors.toList());

        return ResponseEntity.ok(shopList);
    }

    @GetMapping(path = "/bySeller")
    public ResponseEntity<?> fetchShopBySeller(@RequestParam String seller){

        Shop shop = genericService.shopBySellerName(seller);

        if(null == shop) throw new InventoryAPIResourceNotFoundException("Shop not found",
                "Shop was not retrieved with the seller name/email: " + seller, null);

        return ResponseEntity.ok(shop);
    }

    @DeleteMapping(path = "/delete/byId")
    public ResponseEntity<?> deleteShopByShopId(@RequestParam Long shopId){

        Shop deletedShop = shopServices.deleteShop(shopId);

        if (null == deletedShop) throw new InventoryAPIOperationException("Shop not deleted",
                "Shop was not deleted successfully, review your inputs and try again", null);

        return ResponseEntity.ok(deletedShop);
    }
}
