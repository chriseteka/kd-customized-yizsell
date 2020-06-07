package com.chrisworks.personal.inventorysystem.Backend.Entities.listeners;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Invoice;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Loyalty;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ReturnedStockRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.InvoiceRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.LoyaltyRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ExchangedStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.PreRemove;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Chris_Eteka
 * @since 6/7/2020
 * @email chriseteka@gmail.com
 */
@Component
public class CustomerListener {

    @Autowired private ReturnedStockRepository returnedStockRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private LoyaltyRepository loyaltyRepository;
    @Autowired private ExchangedStockRepository exchangedStockRepository;

    @PreRemove
    private void relinquishCustomerFromTies(Customer customer){

        System.out.println("Came here");
        System.out.println(invoiceRepository.findAll());
        //Remove this customer from every invoices it has attached itself with
//        List<Invoice> updatedInvoices = invoiceRepository
//            .findAllByCustomerId(customer)
//            .stream().peek(invoice -> invoice.setCustomerId(null))
//            .collect(Collectors.toList());
//        invoiceRepository.saveAll(updatedInvoices);

        System.out.println("Came to returned stock");
        //Remove this customer from every returns it has attached itself with
        returnedStockRepository.saveAll
            (returnedStockRepository
                .findAllByCustomerId(customer)
                    .stream().peek(returnedStock -> returnedStock.setCustomerId(null))
                    .collect(Collectors.toList())
            );

        //Remove this customer from every exchanges it has attached itself with
        exchangedStockRepository.saveAll
            (exchangedStockRepository
                .findAllByCustomerId(customer)
                    .stream().peek(exchangedStock -> exchangedStock.setCustomerId(null))
                    .collect(Collectors.toList())
            );

        //Remove this customer from every loyalty it has attached itself with
        Loyalty loyalty = loyaltyRepository.findDistinctByCustomers(customer);
        if (loyalty.getCustomers().removeIf(c -> c.equals(customer)))
            loyaltyRepository.save(loyalty);
    }
}
