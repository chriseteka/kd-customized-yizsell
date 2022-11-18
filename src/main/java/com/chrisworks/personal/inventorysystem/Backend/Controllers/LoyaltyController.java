package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Loyalty;
import com.chrisworks.personal.inventorysystem.Backend.Services.LoyaltyServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Entities.ListWrapper.prepareResponse;

/**
 * @author Chris_Eteka
 * @since 1/16/2020
 * @email chriseteka@gmail.com
 */
@RestController
@RequestMapping("/loyalty")
public class LoyaltyController {

    private final LoyaltyServices loyaltyServices;

    @Autowired
    public LoyaltyController(LoyaltyServices loyaltyServices) {
        this.loyaltyServices = loyaltyServices;
    }

    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createLoyaltyPlan(@RequestBody @Valid Loyalty loyalty){

        return new ResponseEntity<>(loyaltyServices.createEntity(loyalty), HttpStatus.CREATED);
    }

    @PutMapping(path = "/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateLoyaltyPlan(@RequestParam Long loyaltyId,
                                               @RequestBody @Valid Loyalty loyalty){

        return ResponseEntity.ok(loyaltyServices.updateEntity(loyaltyId, loyalty));
    }

    @PutMapping(path = "/add/customer", produces = "application/json")
    public ResponseEntity<?> addCustomerToPlan(@RequestParam Long customerId,
                                               @RequestParam Long loyaltyId){

        return ResponseEntity.ok(loyaltyServices.addCustomerToLoyaltyPlan(loyaltyId, customerId));
    }

    @PutMapping(path = "/remove/customer", produces = "application/json")
    public ResponseEntity<?> removeCustomerFromPlan(@RequestParam Long customerId,
                                               @RequestParam Long loyaltyId){

        return ResponseEntity.ok(loyaltyServices.removeCustomerFromPlan(loyaltyId, customerId));
    }

    @GetMapping(path = "/byId", produces = "application/json")
    public  ResponseEntity<?> fetchLoyaltyById(@RequestParam Long loyaltyId){

        return ResponseEntity.ok(loyaltyServices.getSingleEntity(loyaltyId));
    }

    @GetMapping(path = "/byCustomerId", produces = "application/json")
    public  ResponseEntity<?> fetchLoyaltyByCustomerId(@RequestParam Long customerId){

        return ResponseEntity.ok(loyaltyServices.findLoyaltyPlanByCustomerId(customerId));
    }

    @GetMapping(path = "/all", produces = "application/json")
    public  ResponseEntity<?> fetchAllLoyaltyPlan(@RequestParam int page, @RequestParam int size){

        List<Loyalty> loyaltyList = loyaltyServices.getEntityList()
                .stream()
                .sorted(Comparator.comparing(Loyalty::getCreatedDate)
                    .thenComparing(Loyalty::getCreatedTime).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(prepareResponse(loyaltyList, page, size));
    }

    @GetMapping(path = "/customers", produces = "application/json")
    public  ResponseEntity<?> fetchAllLoyalCustomers(@RequestParam int page, @RequestParam int size){

        List<Customer> loyalCustomerList = loyaltyServices.fetchAuthenticatedUserLoyalCustomers();

        return ResponseEntity.ok(prepareResponse(loyalCustomerList, page, size));
    }

    @DeleteMapping(path = "/delete/byId", produces = "application/json")
    public ResponseEntity<?> deleteLoyaltyById(@RequestParam Long loyaltyId){

        return ResponseEntity.ok(loyaltyServices.deleteEntity(loyaltyId));
    }
}
