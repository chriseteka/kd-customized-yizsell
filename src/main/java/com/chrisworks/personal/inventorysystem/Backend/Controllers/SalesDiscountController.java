package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.SalesDiscount;
import com.chrisworks.personal.inventorysystem.Backend.Services.SalesDiscountServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Entities.ListWrapper.prepareResponse;

@RestController
@RequestMapping("/sales/discount")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SalesDiscountController {

    private final SalesDiscountServices salesDiscountServices;

    @Autowired
    public SalesDiscountController(SalesDiscountServices salesDiscountServices) {
        this.salesDiscountServices = salesDiscountServices;
    }

    @GetMapping(path = "/all", produces = "application/json")
    public ResponseEntity<?> allSalesDiscounts(@RequestParam int page, @RequestParam int size){

        List<SalesDiscount> salesDiscounts = salesDiscountServices.fetchAllSalesDiscount()
                .stream()
                .sorted(Comparator.comparing(SalesDiscount::getCreatedDate).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(prepareResponse(salesDiscounts, page, size));
    }

    @GetMapping(path = "/all/byType", produces = "application/json")
    public ResponseEntity<?> allSalesDiscountsByType(@RequestParam String type, @RequestParam int page,
                                               @RequestParam int size){

        List<SalesDiscount> salesDiscounts = salesDiscountServices.fetchAllSalesDiscountByType(type)
                .stream()
                .sorted(Comparator.comparing(SalesDiscount::getCreatedDate).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(prepareResponse(salesDiscounts, page, size));
    }

    @GetMapping(path = "/all/byDate", produces = "application/json")
    public ResponseEntity<?> allSalesDiscountsByDate(@RequestParam Date date, @RequestParam int page,
                                                     @RequestParam int size){

        List<SalesDiscount> salesDiscounts = salesDiscountServices.fetchAllSalesDiscountByDate(date)
                .stream()
                .sorted(Comparator.comparing(SalesDiscount::getCreatedDate).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(prepareResponse(salesDiscounts, page, size));
    }

    @GetMapping(path = "/all/byInvoice", produces = "application/json")
    public ResponseEntity<?> allSalesDiscountsByInvoice(@RequestParam String invoiceNumber, @RequestParam int page,
                                                     @RequestParam int size){

        List<SalesDiscount> salesDiscounts = salesDiscountServices.fetchAllSalesDiscountByInvoice(invoiceNumber)
                .stream()
                .sorted(Comparator.comparing(SalesDiscount::getCreatedDate).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(prepareResponse(salesDiscounts, page, size));
    }

    @GetMapping(path = "/all/bySeller", produces = "application/json")
    public ResponseEntity<?> allSalesDiscountsBySeller(@RequestParam String sellerEmail, @RequestParam int page,
                                                     @RequestParam int size){

        List<SalesDiscount> salesDiscounts = salesDiscountServices.fetchAllBySellerEmail(sellerEmail)
                .stream()
                .sorted(Comparator.comparing(SalesDiscount::getCreatedDate).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(prepareResponse(salesDiscounts, page, size));
    }
}
