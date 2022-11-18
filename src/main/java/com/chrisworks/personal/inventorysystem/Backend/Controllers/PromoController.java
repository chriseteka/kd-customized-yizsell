package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Promo;
import com.chrisworks.personal.inventorysystem.Backend.Services.PromoServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/promo")
@RequiredArgsConstructor
public class PromoController {

    private final PromoServices promoServices;

    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createPromo(@RequestBody @Valid Promo promo){

        return ResponseEntity.ok(promoServices.createEntity(promo));
    }

    @PutMapping(path = "/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updatePromo(@RequestParam Long promoId,
                                         @RequestBody @Valid Promo promo){

        return ResponseEntity.ok(promoServices.updateEntity(promoId, promo));
    }

    @PutMapping(path = "/add/stock", produces = "application/json")
    public ResponseEntity<?> addStockToPromo(@RequestParam Long promoId,
                                         @RequestParam Long stockId){

        return ResponseEntity.ok(promoServices.addStockToPromo(promoId, stockId));
    }

    @PutMapping(path = "/remove/stock", produces = "application/json")
    public ResponseEntity<?> removeStockFromPromo(@RequestParam Long promoId,
                                         @RequestParam Long stockId){

        return ResponseEntity.ok(promoServices.removeStockFromPromo(promoId, stockId));
    }

    @GetMapping(path = "/find", produces = "application/json")
    public ResponseEntity<?> fetchPromoById(@RequestParam Long promoId){

        return ResponseEntity.ok(promoServices.getSingleEntity(promoId));
    }

    @GetMapping(path = "/all", produces = "application/json")
    public ResponseEntity<?> fetchAllPromo(){

        return ResponseEntity.ok(promoServices.getEntityList());
    }

    @DeleteMapping(path = "/delete", produces = "application/json")
    public ResponseEntity<?> deletePromo(@RequestParam Long promoId){

        return ResponseEntity.ok(promoServices.deleteEntity(promoId));
    }


}
