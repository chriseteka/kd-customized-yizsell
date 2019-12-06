package com.chrisworks.personal.inventorysystem.Backend.Controllers.SellerController;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Services.GenericService;
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

    private GenericService genericService;

    @Autowired
    public SellerController(SellerServices sellerServices, GenericService genericService) {
        this.sellerServices = sellerServices;
        this.genericService = genericService;
    }

    @PostMapping(path = "/createSeller", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createSeller(@RequestBody @Valid Seller seller){

        return ResponseEntity.ok(sellerServices.createSeller(seller));
    }

    @GetMapping(path = "/seller/id")
    public ResponseEntity<?> fetchSellerById(@RequestParam Long id){

        Seller seller = sellerServices.fetchSellerById(id);

        return seller != null ? ResponseEntity.ok(seller) : ResponseEntity.notFound().build();
    }

    @GetMapping(path = "/seller/name")
    public ResponseEntity<?> fetchSellerByName(@RequestParam String sellerName){

        Seller seller = sellerServices.fetchSellerByName(sellerName);

        return seller != null ? ResponseEntity.ok(seller) : ResponseEntity.notFound().build();
    }

    @GetMapping(path = "/sellers")
    public ResponseEntity<?> fetchAllSellers(){

        List<Seller> allSellers = sellerServices.allSellers();

        return (allSellers != null && !allSellers.isEmpty()) ? ResponseEntity.ok(allSellers) : ResponseEntity.notFound().build();
    }
}
