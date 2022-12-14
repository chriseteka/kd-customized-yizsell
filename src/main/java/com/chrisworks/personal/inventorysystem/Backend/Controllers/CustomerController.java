package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Services.CustomerService;
import com.chrisworks.personal.inventorysystem.Backend.Utility.PDFMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Entities.ListWrapper.prepareResponse;
import static com.chrisworks.personal.inventorysystem.Backend.Utility.GeneratePDFReport.generatePDFReport;

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
    public ResponseEntity<?> fetchAllCustomers(@RequestParam int page, @RequestParam int size){

        List<Customer> customerList = customerService.fetchAllCustomers()
                .stream()
                .sorted(Comparator.comparing(Customer::getCreatedDate)
                        .thenComparing(Customer::getCreatedTime).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(prepareResponse(customerList,page, size));
    }

    @GetMapping(path = "/withDebt")
    public ResponseEntity<?> fetchAllCustomersWithDebt(@RequestParam int page, @RequestParam int size){

        List<Customer> customerList = customerService.fetchAllCustomersWithDebt()
                .stream()
                .sorted(Comparator.comparing(Customer::getCreatedDate)
                        .thenComparing(Customer::getCreatedTime).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(prepareResponse(customerList,page, size));
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

    @GetMapping(path = "/debt")
    public ResponseEntity<?> fetchCustomerDebt(@RequestParam Long customerId){

        return ResponseEntity.ok(customerService.fetchCustomerDebt(customerId));
    }

    @GetMapping(path = "/print")
    public ResponseEntity<?> printTrial(@RequestBody PDFMap pdfMap){

        ByteArrayResource resource = new ByteArrayResource(generatePDFReport(pdfMap));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }
}
