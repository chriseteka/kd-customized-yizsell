package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ExchangedStockRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.InvoiceRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.LoyaltyRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ReturnedStockRepository;
import com.chrisworks.personal.inventorysystem.Backend.Services.PreEmptiveServices.CustomerPreEmptives;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Chris_Eteka
 * @since 6/8/2020
 * @email chriseteka@gmail.com
 */
@Service
@RequiredArgsConstructor
public class UtilServices implements CustomerPreEmptives {

    private final ReturnedStockRepository returnedStockRepository;
    private final InvoiceRepository invoiceRepository;
    private final LoyaltyRepository loyaltyRepository;
    private final ExchangedStockRepository exchangedStockRepository;

    @Transactional
    @Override
    public void detachCustomersFromObjects(Customer customer){

        //Remove this customer from every invoices it has attached itself with
        List<Invoice> invoices = invoiceRepository
                .findAllByCustomerId(customer)
                .stream().peek(invoice -> {
                    invoice.setCustomerId(null);
                    invoice.setPaymentModeVal(String.valueOf(invoice.getPaymentModeValue()));
                })
                .collect(Collectors.toList());

        //Remove this customer from every returns it has attached itself with
        List<ReturnedStock> returnedStockList = returnedStockRepository
                .findAllByCustomerId(customer)
                .stream().peek(returnedStock -> returnedStock.setCustomerId(null))
                .collect(Collectors.toList());

        //Remove this customer from every exchanges it has attached itself with
        List<ExchangedStock> exchangedStockList = exchangedStockRepository
                .findAllByCustomerId(customer)
                .stream().peek(exchangedStock -> exchangedStock.setCustomerId(null))
                .collect(Collectors.toList());

        //Remove this customer from every loyalty it has attached itself with
        Loyalty loyalty = loyaltyRepository.findDistinctByCustomers(customer);
        if (loyalty != null && loyalty.getCustomers().removeIf(c -> c.equals(customer)))
            loyaltyRepository.save(loyalty);

        if (!invoices.isEmpty()) invoiceRepository.saveAll(invoices);
        if (!returnedStockList.isEmpty())returnedStockRepository.saveAll(returnedStockList);
        if (!exchangedStockList.isEmpty()) exchangedStockRepository.saveAll(exchangedStockList);
    }

}
