package com.chrisworks.personal.inventorysystem.Backend.Controllers.BusinessOwnerController;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import com.chrisworks.personal.inventorysystem.Backend.Services.BusinessOwnerServices.BusinessOwnerServices;
import com.chrisworks.personal.inventorysystem.Backend.Services.GenericServices.GenericService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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

    @Autowired
    public BusinessOwnerController(BusinessOwnerServices businessOwnerServices, GenericService genericService) {
        this.businessOwnerServices = businessOwnerServices;
        this.genericService = genericService;
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
}
