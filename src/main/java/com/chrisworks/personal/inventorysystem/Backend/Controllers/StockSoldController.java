package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockSold;
import com.chrisworks.personal.inventorysystem.Backend.Services.StockSoldServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Entities.ListWrapper.prepareResponse;

/**
 * @author Chris_Eteka
 * @since 12/31/2019
 * @email chriseteka@gmail.com
 */
@RestController
@RequestMapping("/stockSold/all")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class StockSoldController {

    private final StockSoldServices stockSoldServices;

    @Autowired
    public StockSoldController(StockSoldServices stockSoldServices) {
        this.stockSoldServices = stockSoldServices;
    }

    @GetMapping
    public ResponseEntity<?> fetchAllStockSoldByAuthUser(@RequestParam int page, @RequestParam int size){

        List<StockSold> stockSoldList = stockSoldServices.fetchAllStockSoldByAuthenticatedUser()
                .stream()
                .sorted(Comparator.comparing(StockSold::getCreatedDate).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(prepareResponse(stockSoldList, page, size));
    }

    @GetMapping(path = "/byInvoiceId")
    public ResponseEntity<?> fetchAllStockSoldByInvoiceId(@RequestParam Long invoiceId){

        return ResponseEntity.ok(stockSoldServices.fetchAllStockSoldByInvoiceId(invoiceId));
    }

    @GetMapping(path = "/byInvoiceNumber")
    public ResponseEntity<?> fetchAllStockSoldByInvoiceNumber(@RequestParam String invoiceNumber){

        return ResponseEntity.ok(stockSoldServices.fetchAllStockSoldByByInvoiceNumber(invoiceNumber));
    }

    @GetMapping(path = "/bySeller")
    public ResponseEntity<?> fetchAllStockSoldBySeller(@RequestParam String sellerMail){

        return ResponseEntity.ok(stockSoldServices.fetchAllStockSoldBySellerEmail(sellerMail));
    }

    @GetMapping(path = "/byDate")
    public ResponseEntity<?> fetchAllStockSoldByDate(@RequestParam Date date){

        return ResponseEntity.ok(stockSoldServices.fetchAllStockSoldByDate(date));
    }

    @GetMapping(path = "/byShop")
    public ResponseEntity<?> fetchAllStockSoldInShop(@RequestParam Long shopId){

        return ResponseEntity.ok(stockSoldServices.fetchAllStockSoldByShop(shopId));
    }

    @GetMapping(path = "/byStockName")
    public ResponseEntity<?> fetchAllStockSoldByStockName(@RequestParam String stockName){

        return ResponseEntity.ok(stockSoldServices.fetchAllStockSoldByStockName(stockName));
    }

    @GetMapping(path = "/byStockCategory")
    public ResponseEntity<?> fetchAllStockSoldByStockCategory(@RequestParam String stockCategory){

        return ResponseEntity.ok(stockSoldServices.fetchAllStockSoldByStockCategory(stockCategory));
    }

    @GetMapping(path = "/toCustomer")
    public ResponseEntity<?> fetchAllStockSoldToCustomer(@RequestParam Long customerId){

        return ResponseEntity.ok(stockSoldServices.fetchAllStockSoldToCustomer(customerId));
    }
}
