package com.chrisworks.personal.inventorysystem.Backend.Controllers.SellerController;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Services.BusinessOwnerServices.BusinessOwnerServices;
import com.chrisworks.personal.inventorysystem.Backend.Services.GenericServices.GenericService;
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

    private BusinessOwnerServices businessOwnerServices;

    private GenericService genericService;

    @Autowired
    public SellerController(BusinessOwnerServices businessOwnerServices, GenericService genericService) {
        this.businessOwnerServices = businessOwnerServices;
        this.genericService = genericService;
    }

    @PostMapping(path = "/createSeller", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createSeller(@RequestBody @Valid Seller seller){

        return ResponseEntity.ok(businessOwnerServices.createSeller(seller));
    }

    @GetMapping(path = "/seller/id")
    public ResponseEntity<?> fetchSellerById(@RequestParam Long id){

        Seller seller = businessOwnerServices.fetchSellerById(id);

        return seller != null ? ResponseEntity.ok(seller) : ResponseEntity.notFound().build();
    }

    @GetMapping(path = "/seller/name")
    public ResponseEntity<?> fetchSellerByName(@RequestParam String sellerName){

        Seller seller = businessOwnerServices.fetchSellerByName(sellerName);

        return seller != null ? ResponseEntity.ok(seller) : ResponseEntity.notFound().build();
    }

    @GetMapping(path = "/sellers")
    public ResponseEntity<?> fetchAllSellers(){

        List<Seller> allSellers = businessOwnerServices.allSellers();

        return (allSellers != null && !allSellers.isEmpty()) ? ResponseEntity.ok(allSellers) : ResponseEntity.notFound().build();
    }
}
