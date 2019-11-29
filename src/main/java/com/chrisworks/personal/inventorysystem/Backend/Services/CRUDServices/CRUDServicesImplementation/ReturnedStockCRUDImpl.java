package com.chrisworks.personal.inventorysystem.Backend.Services.CRUDServices.CRUDServicesImplementation;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ReturnedStock;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.CustomerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ReturnedStockRepository;
import com.chrisworks.personal.inventorysystem.Backend.Services.CRUDServices.ReturnedStockServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Chris_Eteka
 * @since 11/29/2019
 * @email chriseteka@gmail.com
 */
@Service
public class ReturnedStockCRUDImpl implements ReturnedStockServices {

    private final ReturnedStockRepository returnedStockRepository;

    private final CustomerRepository customerRepository;

    @Autowired
    public ReturnedStockCRUDImpl(ReturnedStockRepository returnedStockRepository, CustomerRepository customerRepository) {
        this.returnedStockRepository = returnedStockRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public ReturnedStock fetchStockReturnedByInvoiceNumberAndStockName(String invoiceNumber, String stockName) {

        return returnedStockRepository.findAllByInvoiceIdAndStockName(invoiceNumber, stockName);
    }

    @Override
    public List<ReturnedStock> fetchAllStockReturnedTo(String userFullName) {

        return returnedStockRepository.findAllByCreatedBy(userFullName);
    }

    @Override
    public List<ReturnedStock> fetchAllStockReturnedWithInvoice(String invoiceNumber) {

        return returnedStockRepository.findAllByInvoiceId(invoiceNumber);
    }

    @Override
    public List<ReturnedStock> fetchAllStockReturnedByCustomer(Long customerId) {

        AtomicReference<Customer> customerRetrieved = new AtomicReference<>();

        customerRepository.findById(customerId).ifPresent(customerRetrieved::set);

        if (customerRetrieved.get() == null) {

            //throw error and return
            return null;
        }

        return returnedStockRepository.findAllByCustomerId(customerRetrieved.get());
    }

    @Override
    public List<ReturnedStock> fetchAllApprovedReturns() {

        return returnedStockRepository.findAllByApprovedTrue();
    }

    @Override
    public List<ReturnedStock> fetchAllUnapprovedReturns() {

        return returnedStockRepository.findAllByApprovedFalse();
    }

    @Override
    public List<ReturnedStock> fetchAllReturnsWithin(Date startDate, Date toDate) {

        return returnedStockRepository.findAllByCreatedDateIsBetween(startDate, toDate);
    }
}
