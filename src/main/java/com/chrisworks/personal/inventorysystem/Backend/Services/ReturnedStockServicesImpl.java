package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ReturnedStock;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.CustomerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ReturnedStockRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ShopRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;

/**
 * @author Chris_Eteka
 * @since 11/29/2019
 * @email chriseteka@gmail.com
 */
@Service
public class ReturnedStockServicesImpl implements ReturnedStockServices {

    private final ReturnedStockRepository returnedStockRepository;

    private final CustomerRepository customerRepository;

    private final ShopRepository shopRepository;

    private final GenericService genericService;

    @Autowired
    public ReturnedStockServicesImpl(ReturnedStockRepository returnedStockRepository, CustomerRepository customerRepository,
                                     ShopRepository shopRepository, GenericService genericService) {
        this.returnedStockRepository = returnedStockRepository;
        this.customerRepository = customerRepository;
        this.shopRepository = shopRepository;
        this.genericService = genericService;
    }

    @Override
    public ReturnedStock fetchStockReturnedByInvoiceNumberAndStockName(String invoiceNumber, String stockName) {

        return returnedStockRepository.findAllByInvoiceIdAndStockName(invoiceNumber, stockName);
    }

    @Override
    public List<ReturnedStock> fetchAllStockReturnedTo(String userFullName) {

        return fetchAllReturnedStocks()
                .stream()
                .filter(returnedStock -> returnedStock.getCreatedBy().equalsIgnoreCase(userFullName))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReturnedStock> fetchAllStockReturnedToShop(Long shopId) {

        return genericService.shopByAuthUserId()
                .stream()
                .filter(shop -> shop.getShopId().equals(shopId))
                .map(returnedStockRepository::findAllByShop)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReturnedStock> fetchAllStockReturnedWithInvoice(String invoiceNumber) {

        return fetchAllReturnedStocks()
                .stream()
                .filter(returnedStock -> returnedStock.getInvoiceId().equalsIgnoreCase(invoiceNumber))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReturnedStock> fetchAllStockReturnedByCustomer(Long customerId) {

        return fetchAllReturnedStocks()
                .stream()
                .filter(returnedStock -> returnedStock.getCustomerId().getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReturnedStock> fetchAllApprovedReturns() {

        return fetchAllReturnedStocks()
                .stream()
                .filter(ReturnedStock::getApproved)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReturnedStock> fetchAllReturnedStocks() {

        return genericService.sellersByAuthUserId()
                .stream()
                .map(Seller::getSellerEmail)
                .map(returnedStockRepository::findAllByCreatedBy)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReturnedStock> fetchAllUnapprovedReturnsCreatedBy(String createdBy) {

        return fetchAllUnApprovedReturnSales()
                .stream()
                .filter(returnedStock -> returnedStock.getCreatedBy().equalsIgnoreCase(createdBy))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReturnedStock> fetchAllReturnsWithin(Date startDate, Date toDate) {

        return fetchAllReturnedStocks()
                .stream()
                .filter(returnedStock -> returnedStock.getCreatedDate().compareTo(startDate) >= 0
                        && toDate.compareTo(returnedStock.getCreatedDate()) >= 0)
                .collect(Collectors.toList());
    }

    @Override
    public ReturnedStock deleteReturnedStock(Long returnedStockId) {

        if (AuthenticatedUserDetails.getAccount_type() == null ||
                !AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return returnedStockRepository.findById(returnedStockId).map(returnedStock -> {

            boolean match = genericService.sellersByAuthUserId()
                    .stream()
                    .map(Seller::getSellerEmail)
                    .anyMatch(sellerName -> sellerName.equalsIgnoreCase(returnedStock.getCreatedBy()));

            if (match) {
                returnedStockRepository.delete(returnedStock);
                return returnedStock;
            }
            else throw new InventoryAPIOperationException("Operation not allowed",
                    "You cannot delete an expense not created by you or any of your sellers", null);
        }).orElse(null);
    }

    @Override
    public ReturnedStock approveReturnSales(Long returnSaleId) {

        ReturnedStock returnedStockFound = fetchAllReturnedStocks()
                .stream()
                .filter(returnedStock -> returnedStock.getReturnedStockId().equals(returnSaleId))
                .collect(toSingleton());

        if (returnedStockFound == null) throw new InventoryAPIResourceNotFoundException
                ("Returned stock not found", "Returned stock with id: " + returnSaleId + "was not found in your list of" +
                        " unapproved returned stock", null);

        returnedStockFound.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
        returnedStockFound.setApproved(true);
        returnedStockFound.setApprovedDate(new Date());

        return returnedStockRepository.save(returnedStockFound);
    }

    @Override
    public List<ReturnedStock> fetchAllUnApprovedReturnSales() {

        return fetchAllReturnedStocks()
                .stream()
                .filter(returnedStock -> !returnedStock.getApproved())
                .collect(Collectors.toList());
    }
}
