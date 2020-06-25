package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Invoice;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockSold;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.CustomerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.InvoiceRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.StockSoldRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
@Service
public class StockSoldServicesImpl implements StockSoldServices {

    private final StockSoldRepository stockSoldRepository;

    private final InvoiceRepository invoiceRepository;

    private final GenericService genericService;

    private final CustomerRepository customerRepository;

    @Autowired
    public StockSoldServicesImpl(StockSoldRepository stockSoldRepository, GenericService genericService,
                                 InvoiceRepository invoiceRepository, CustomerRepository customerRepository) {
        this.stockSoldRepository = stockSoldRepository;
        this.genericService = genericService;
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public List<StockSold> fetchAllStockSoldByAuthenticatedUser() {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        List<StockSold> stockSoldByAuthUser = stockSoldRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER))
            return stockSoldByAuthUser;

        stockSoldByAuthUser.addAll(genericService.sellersByAuthUserId()
                .stream()
                .filter(seller -> seller.getShop() != null)
                .map(Seller::getSellerEmail)
                .map(stockSoldRepository::findAllByCreatedBy)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList()));

        return stockSoldByAuthUser;
    }

    @Override
    public List<StockSold> fetchAllStockSoldByInvoiceId(Long invoiceId) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return invoiceRepository.findById(invoiceId).map(invoiceFound -> {

            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)
                    && invoiceFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                return new ArrayList<>(invoiceFound.getStockSold());

            else if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

                if (invoiceFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                    return new ArrayList<>(invoiceFound.getStockSold());
                else {

                    boolean match = genericService.sellersByAuthUserId()
                            .stream()
                            .map(Seller::getSellerEmail)
                            .anyMatch(sellerName -> invoiceFound.getCreatedBy().endsWith(sellerName));

                    if (match) return new ArrayList<>(invoiceFound.getStockSold());
                    else throw new InventoryAPIOperationException("Not your invoice",
                            "The invoice with id: " + invoiceId + " was not found in your list of invoices", null);
                }
            }
            throw  new InventoryAPIOperationException("Not your invoice",
                    "The invoice with id: " + invoiceId + " was not found in your list of invoices", null);
        }).orElse(new ArrayList<>(Collections.emptyList()));
    }

    @Override
    public List<StockSold> fetchAllStockSoldByByInvoiceNumber(String invoiceNumber) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        Invoice invoiceFound = invoiceRepository.findDistinctByInvoiceNumber(invoiceNumber);

        if (null == invoiceFound || null == invoiceFound.getStockSold()
                || invoiceFound.getStockSold().isEmpty())
            throw new InventoryAPIResourceNotFoundException("Not found",
                        "Stock sold by invoice number: " + invoiceNumber +
                                " was not found, review your inputs and try again", null);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)
                && invoiceFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
            return new ArrayList<>(invoiceFound.getStockSold());

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            if (invoiceFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                return new ArrayList<>(invoiceFound.getStockSold());
            else {

                boolean match = genericService.sellersByAuthUserId()
                        .stream()
                        .map(Seller::getSellerEmail)
                        .anyMatch(sellerName -> invoiceFound.getCreatedBy().endsWith(sellerName));

                if (match) return new ArrayList<>(invoiceFound.getStockSold());
                else throw new InventoryAPIOperationException("Not your invoice",
                        "The invoice number: " + invoiceNumber + " was not found in your list of invoices", null);
            }
        }

        else throw new InventoryAPIOperationException("Not your invoice",
                "The invoice number: " + invoiceNumber + " was not found in your list of invoices", null);
    }

    @Override
    public List<StockSold> fetchAllStockSoldBySellerEmail(String sellerEmail) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        List<Invoice> invoiceBySellerEmail = invoiceRepository.findAllByCreatedBy(sellerEmail);

        if (invoiceBySellerEmail == null || invoiceBySellerEmail.isEmpty())
            throw new InventoryAPIResourceNotFoundException("Not found",
                    "No invoice was retrieved for the seller email passed, review your inputs and try again", null);

        if (sellerEmail.equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
            return invoiceBySellerEmail
                    .stream()
                    .map(Invoice::getStockSold)
                    .flatMap(Set::parallelStream)
                    .collect(Collectors.toList());

        boolean match = genericService.sellersByAuthUserId()
                .stream()
                .map(Seller::getSellerEmail)
                .anyMatch(sellerEmail::equalsIgnoreCase);

        if (match) return invoiceBySellerEmail
                            .stream()
                            .map(Invoice::getStockSold)
                            .flatMap(Set::parallelStream)
                            .collect(Collectors.toList());

        throw new InventoryAPIOperationException("Not your seller",
                "The seller email passed was not created by you, hence you cannot see sales related to the seller.", null);
    }

    @Override
    public List<StockSold> fetchAllStockSoldByDate(Date date) {

        return fetchAllStockSoldByAuthenticatedUser()
                .stream()
                .filter(stockSold -> stockSold.getCreatedDate().compareTo(date) == 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockSold> fetchAllStockSoldByShop(Long shopId) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        List<Seller> sellerList = genericService.sellersByAuthUserId();

        boolean match = sellerList
                .stream()
                .map(Seller::getShop)
                .map(Shop::getShopId)
                .anyMatch(id -> id.equals(shopId));

        if (!match) throw new InventoryAPIOperationException("Shop not yours",
                "Shop with id: " + shopId + " is not found in the list of your shops", null);

        return sellerList
                .stream()
                .filter(seller -> seller.getShop() != null &&
                        seller.getShop().getShopId() == shopId)
                .map(invoiceRepository::findAllBySeller)
                .flatMap(List::parallelStream)
                .map(Invoice::getStockSold)
                .flatMap(Set::parallelStream)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockSold> fetchAllStockSoldByStockName(String stockName) {

        return fetchAllStockSoldByAuthenticatedUser()
                .stream()
                .filter(stockSold -> stockSold.getStockName().equalsIgnoreCase(stockName))
                .collect(Collectors.toList());
    }

    @Override
    public List<StockSold> fetchAllStockSoldByStockCategory(String stockCategory) {

        return fetchAllStockSoldByAuthenticatedUser()
                .stream()
                .filter(stockSold -> stockSold.getStockCategory().equalsIgnoreCase(stockCategory))
                .collect(Collectors.toList());
    }

    @Override
    public List<StockSold> fetchAllStockSoldToCustomer(Long customerId) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return customerRepository.findById(customerId).map(customer -> {

            List<Invoice> invoiceList = invoiceRepository.findAllByCustomerId(customer);

            if (null == invoiceList || invoiceList.isEmpty())
                throw new InventoryAPIResourceNotFoundException("Not found",
                        "No stock sold was found for the customer id: " + customerId, null);

            List<Shop> shopList = genericService.shopByAuthUserId();
            return invoiceList
                    .stream()
                    .filter(invoice -> shopList.contains(invoice.getSeller().getShop())
                            || invoice.getCreatedBy()
                            .equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                    .map(Invoice::getStockSold)
                    .flatMap(Set::parallelStream)
                    .collect(Collectors.toList());


        }).orElse(new ArrayList<>(Collections.emptyList()));
    }
}
