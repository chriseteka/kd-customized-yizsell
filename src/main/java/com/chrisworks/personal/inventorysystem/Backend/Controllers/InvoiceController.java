package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Invoice;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Services.InvoiceServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoice")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class InvoiceController {

    private final InvoiceServices invoiceServices;

    @Autowired
    public InvoiceController(InvoiceServices invoiceServices) {
        this.invoiceServices = invoiceServices;
    }

    @GetMapping(path = "/byId")
    public ResponseEntity<?> getInvoiceById(@RequestParam Long invoiceId){

        Invoice invoice = invoiceServices.getSingleEntity(invoiceId);

        if (null == invoice) throw new InventoryAPIResourceNotFoundException("Invoice not found",
                "Invoice with id: " + invoiceId + " was not found", null);

        return ResponseEntity.ok(invoice);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<?> getAllInvoices(){

        return ResponseEntity.ok(invoiceServices.getEntityList());
    }

    @DeleteMapping(path = "/byId")
    public ResponseEntity<?> deleteIncomeById(@RequestParam Long invoiceId){

        Invoice invoice = invoiceServices.deleteEntity(invoiceId);

        if (null == invoice) throw new InventoryAPIResourceNotFoundException("Income not deleted",
                "Income with id: " + invoiceId + " was not deleted.", null);

        return ResponseEntity.ok(invoice);
    }
}
