package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/customer")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createCustomer(@RequestBody @Valid Customer customer){

        Customer newCustomer = customerService.createCustomer(customer);

        if (null == newCustomer) throw new InventoryAPIOperationException("Could not create customer",
                "Customer was unable to be created, please try again", null);

        return ResponseEntity.ok(newCustomer);
    }

    @GetMapping(path = "/byCreator")
    public ResponseEntity<?> fetchAllCustomersByCreator(@RequestParam String createdBy){

        return ResponseEntity.ok(customerService.fetchAllCustomersByCreator(createdBy));
    }

    @GetMapping(path = "/byPhone")
    public ResponseEntity<?> getCustomerByPhoneNumber(@RequestParam String customerPhoneNumber){

        Customer customer = customerService.fetchCustomerByPhoneNumber(customerPhoneNumber);

        if (null == customer) throw new InventoryAPIResourceNotFoundException("Customer not found",
                "Customer with phone number: " + customerPhoneNumber + " was not found", null);

        return ResponseEntity.ok(customer);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<?> fetchAllCustomers(){

        return ResponseEntity.ok(customerService.fetchAllCustomers());
    }

    @GetMapping(path = "/withDebt")
    public ResponseEntity<?> fetchAllCustomersWithDebt(@RequestParam BigDecimal debtLimit){

        return ResponseEntity.ok(customerService.fetchAllCustomersWithDebt(debtLimit));
    }

    @GetMapping(path = "/withReturns")
    public ResponseEntity<?> fetchAllCustomersWithReturns(){

        return ResponseEntity.ok(customerService.fetchAllCustomersWithReturnedPurchases());
    }

    @PutMapping(path = "/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateCustomerDetails(@RequestParam Long customerId,
                                                   @RequestBody @Valid Customer customer) {

        Customer updatedCustomerDetails = customerService.updateCustomerDetails(customerId, customer);

        if (null == updatedCustomerDetails) throw new InventoryAPIOperationException("Could not update customer",
                "Customer was unable to be updated, please try again", null);

        return ResponseEntity.ok(updatedCustomerDetails);
    }

    @GetMapping(path = "/byShop")
    public ResponseEntity<?> getCustomerByShop(@RequestParam Long shopId){

        return ResponseEntity.ok(customerService.fetchCustomersByShop(shopId));
    }

    @GetMapping(path = "/byId")
    public ResponseEntity<?> getCustomerById(@RequestParam Long customerId){

        Customer customer = customerService.fetchCustomerById(customerId);

        if (null == customer)throw new InventoryAPIResourceNotFoundException("Customer not found",
                "Customer with id: " + customerId + " was not found", null);

        return ResponseEntity.ok(customer);
    }

    @DeleteMapping(path = "/byId")
    public ResponseEntity<?> deleteCustomerById(@RequestParam Long customerId){

        return ResponseEntity.ok(customerService.deleteCustomerById(customerId));
    }
}
