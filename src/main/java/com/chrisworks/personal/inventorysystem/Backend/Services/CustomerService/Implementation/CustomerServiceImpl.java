package com.chrisworks.personal.inventorysystem.Backend.Services.CustomerService.Implementation;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.CustomerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.InvoiceRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ReturnedStockRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ShopRepository;
import com.chrisworks.personal.inventorysystem.Backend.Services.CustomerService.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Chris_Eteka
 * @since 11/28/2019
 * @email chriseteka@gmail.com
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private CustomerRepository customerRepository;

    private ReturnedStockRepository returnedStockRepository;

    private InvoiceRepository invoiceRepository;

    private ShopRepository shopRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, ReturnedStockRepository returnedStockRepository,
                               InvoiceRepository invoiceRepository, ShopRepository shopRepository) {
        this.customerRepository = customerRepository;
        this.returnedStockRepository = returnedStockRepository;
        this.invoiceRepository = invoiceRepository;
        this.shopRepository = shopRepository;
    }

    @Override
    public Customer fetchCustomerByPhoneNumber(String customerPhoneNumber) {

        return customerRepository.findDistinctByCustomerPhoneNumber(customerPhoneNumber);
    }

    @Override
    public List<Customer> fetchAllCustomers() {

        return customerRepository.findAll();
    }

    @Override
    public List<Customer> fetchAllCustomersWithDebt(BigDecimal debtLimit) {

        return invoiceRepository.findAllByDebtGreaterThan(debtLimit)
                .stream().map(Invoice::getCustomerId).collect(Collectors.toList());
    }

    @Override
    public List<Customer> fetchAllCustomersWithReturnedPurchases() {

        return returnedStockRepository.findAll()
                .stream().map(ReturnedStock::getCustomerId).collect(Collectors.toList());
    }

    @Override
    public Customer updateCustomerDetails(Long customerId, Customer customerUpdates) {

        AtomicReference<Customer> updatedCustomer = new AtomicReference<>();

        customerRepository.findById(customerId).ifPresent(customer -> {

            customer.setCustomerFullName(customerUpdates.getCustomerFullName() != null ?
                    customerUpdates.getCustomerFullName() : customer.getCustomerFullName());

            customer.setCustomerFullName(customerUpdates.getCustomerEmail() != null ?
                    customerUpdates.getCustomerEmail() : customer.getCustomerEmail());

            customer.setCustomerFullName(customerUpdates.getCustomerPhoneNumber() != null ?
                    customerUpdates.getCustomerPhoneNumber() : customer.getCustomerPhoneNumber());

            updatedCustomer.set(customerRepository.save(customer));
        });

        return updatedCustomer.get();
    }

    @Override
    public List<Customer> fetchCustomersByShop(Long shopId) {

        Optional<Shop> optionalShop = shopRepository.findById(shopId);

        return optionalShop.map(shop -> shop
                .getSellers()
                .stream()
                .flatMap(seller -> seller.getInvoices().stream()
                        .map(Invoice::getCustomerId))
                .collect(Collectors.toList()))
                .orElse(null);
    }

    @Override
    public Customer deleteCustomerById(Long customerId) {

        AtomicReference<Customer> customerDeleted = new AtomicReference<>(null);

        customerRepository.findById(customerId).ifPresent(customer -> {

            customerDeleted.set(customer);
            customerRepository.delete(customer);
        });

        return customerDeleted.get();
    }


}
