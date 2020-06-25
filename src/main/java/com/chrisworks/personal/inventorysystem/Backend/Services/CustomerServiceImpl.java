package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.*;
import com.chrisworks.personal.inventorysystem.Backend.Services.PreEmptiveServices.CustomerPreEmptives;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;
import static ir.cafebabe.math.utils.BigDecimalUtils.is;

/**
 * @author Chris_Eteka
 * @since 11/28/2019
 * @email chriseteka@gmail.com
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    private final ReturnedStockRepository returnedStockRepository;

    private final InvoiceRepository invoiceRepository;

    private final SellerRepository sellerRepository;

    private final GenericService genericService;

    private final CustomerPreEmptives customerPreEmptives;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, ReturnedStockRepository returnedStockRepository,
                               InvoiceRepository invoiceRepository, SellerRepository sellerRepository,
                               GenericService genericService, UtilServices customerPreEmptives) {
        this.customerRepository = customerRepository;
        this.returnedStockRepository = returnedStockRepository;
        this.invoiceRepository = invoiceRepository;
        this.sellerRepository = sellerRepository;
        this.genericService = genericService;
        this.customerPreEmptives = customerPreEmptives;
    }

    @Override
    public Customer createCustomer(Customer customer) {

        boolean match = fetchAllCustomers()
                .stream()
                .anyMatch(customerFound -> customerFound.getCustomerPhoneNumber()
                        .equalsIgnoreCase(customer.getCustomerPhoneNumber()));

        if (match) throw new InventoryAPIOperationException("Customer already exist",
                "Customer already exist in your shop/business with the phone number: " + customer.getCustomerPhoneNumber()
                        + ", hence it cannot add it to this business' list of customers.", null);

        return genericService.addCustomer(customer);
    }

    @Override
    public Customer fetchCustomerByPhoneNumber(String customerPhoneNumber) {

        return fetchAllCustomers()
                .stream()
                .filter(customer -> customer.getCustomerPhoneNumber().equals(customerPhoneNumber))
                .collect(toSingleton());
    }

    @Override
    public List<Customer> fetchAllCustomersByCreator(String createdBy) {

        return fetchAllCustomers()
                .stream()
                .filter(customer -> customer.getCreatedBy().equalsIgnoreCase(createdBy))
                .collect(Collectors.toList());
    }

    @Override
    public List<Customer> fetchAllCustomers() {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        Set<Customer> customerSet = new HashSet<>(Collections.emptySet());

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)){

            Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());
            List<Seller> sellerList = sellerRepository.findAllByCreatedBy(seller.getCreatedBy());

            customerSet.addAll(sellerList
                    .stream()
                    .map(Seller::getSellerEmail)
                    .map(customerRepository::findAllByCreatedBy)
                    .flatMap(List::parallelStream)
                    .collect(Collectors.toList()));
            customerSet.addAll(customerRepository.findAllByCreatedBy(seller.getCreatedBy()));
        }

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

            customerSet.addAll(genericService.sellersByAuthUserId()
                    .stream()
                    .map(Seller::getSellerEmail)
                    .map(customerRepository::findAllByCreatedBy)
                    .flatMap(List::parallelStream)
                    .collect(Collectors.toList()));

            customerSet.addAll(customerRepository
                    .findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName()));
        }

        return customerSet.stream().filter(customer -> !customer.getCustomerFullName().isEmpty()
                        && !customer.getCustomerPhoneNumber().isEmpty()).collect(Collectors.toList());
    }

    @Override
    public List<Customer> fetchAllCustomersWithDebt() {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Not allowed", "Logged in user cannot perform this operation", null);

        Set<Customer> customersWithDebt = new HashSet<>(Collections.emptySet());

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

            customersWithDebt.addAll(genericService.sellersByAuthUserId().stream()
                    .filter(seller -> seller.getShop() != null)
                    .map(seller -> invoiceRepository.findAllBySellerAndDebtGreaterThan(seller, BigDecimal.ZERO))
                    .flatMap(List::parallelStream)
                    .filter(invoice -> invoice.getCustomerId() != null
                        && !invoice.getCustomerId().getCustomerFullName().isEmpty()
                        && !invoice.getCustomerId().getCustomerPhoneNumber().isEmpty())
                    .map(Invoice::getCustomerId)
                    .collect(Collectors.toList()));

            customersWithDebt.addAll(invoiceRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName())
                    .stream()
                    .filter(invoice -> is(invoice.getDebt()).gt(BigDecimal.ZERO) && invoice.getCustomerId() != null
                            && !invoice.getCustomerId().getCustomerFullName().isEmpty()
                            && !invoice.getCustomerId().getCustomerPhoneNumber().isEmpty())
                    .map(Invoice::getCustomerId)
                    .collect(Collectors.toList()));
        }

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)){

            Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());

            if (null == seller || seller.getShop() == null) throw new InventoryAPIOperationException("Seller not found",
                    "Cannot retrieve debtors for this seller, seller may have not been assigned to a shop or does not exist", null);

            List<Seller> sellerList = sellerRepository.findAllByCreatedBy(seller.getCreatedBy());

            customersWithDebt.addAll(sellerList
                    .stream()
                    .map(s -> invoiceRepository.findAllBySellerAndDebtGreaterThan(s, BigDecimal.ZERO))
                    .flatMap(List::parallelStream)
                    .filter(invoice -> invoice.getCustomerId() != null
                            && !invoice.getCustomerId().getCustomerFullName().isEmpty()
                            && !invoice.getCustomerId().getCustomerPhoneNumber().isEmpty())
                    .map(Invoice::getCustomerId)
                    .collect(Collectors.toList()));
            customersWithDebt.addAll(invoiceRepository.findAllByCreatedBy(seller.getCreatedBy())
                    .stream()
                    .filter(invoice -> is(invoice.getDebt()).gt(BigDecimal.ZERO) && invoice.getCustomerId() != null
                            && !invoice.getCustomerId().getCustomerFullName().isEmpty()
                            && !invoice.getCustomerId().getCustomerPhoneNumber().isEmpty())
                    .map(Invoice::getCustomerId)
                    .collect(Collectors.toList()));
        }

        if (!customersWithDebt.isEmpty())
            customersWithDebt.forEach(customer -> customer.setDebt(computeCustomerDebt(customer)));

        return new ArrayList<>(customersWithDebt);
    }

    @Override
    public List<Customer> fetchAllCustomersWithReturnedPurchases() {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Not allowed", "Logged in user cannot perform this operation", null);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

            return genericService.sellersByAuthUserId().stream()
                    .filter(seller -> seller.getShop() != null)
                    .map(Seller::getShop)
                    .map(returnedStockRepository::findAllByShop)
                    .flatMap(List::parallelStream)
                    .map(ReturnedStock::getCustomerId)
                    .collect(Collectors.toList());

        }

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)){

            Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());

            if (null == seller || seller.getShop() == null) throw new InventoryAPIOperationException("Seller not found",
                    "Cannot retrieve debtors for this seller, seller may have not been assigned to a shop or does not exist", null);

            return returnedStockRepository.findAllByShop(seller.getShop())
                    .stream()
                    .map(ReturnedStock::getCustomerId)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public Customer updateCustomerDetails(Long customerId, Customer customerUpdates) {

        return customerRepository.findById(customerId).map(customer -> {

            if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)
                    && !customer.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Operation not allowed",
                        "You are not allowed to update a customer not created by you", null);

            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

                boolean match = genericService.sellersByAuthUserId()
                        .stream()
                        .map(Seller::getSellerEmail)
                        .anyMatch(sellerName -> sellerName.equalsIgnoreCase(customer.getCreatedBy()));

                if (!match && !customer.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                    throw new InventoryAPIOperationException("Operation not allowed",
                            "You cannot update a customer not created by you or any of your sellers.", null);
            }

            customer.setCustomerFullName(customerUpdates.getCustomerFullName() != null ?
                    customerUpdates.getCustomerFullName() : customer.getCustomerFullName());

            customer.setCustomerEmail(customerUpdates.getCustomerEmail() != null ?
                    customerUpdates.getCustomerEmail() : customer.getCustomerEmail());

            customer.setCustomerPhoneNumber(customerUpdates.getCustomerPhoneNumber() != null ?
                    customerUpdates.getCustomerPhoneNumber() : customer.getCustomerPhoneNumber());

            return customerRepository.save(customer);
        }).orElse(null);

    }

    @Override
    public List<Customer> fetchCustomersByShop(Long shopId) {

        if (AuthenticatedUserDetails.getAccount_type() == null
                || AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return genericService.shopByAuthUserId()
                .stream()
                .filter(shop -> shop.getShopId() == shopId)
                .map(sellerRepository::findAllByShop)
                .flatMap(List::parallelStream)
                .map(invoiceRepository::findAllBySeller)
                .flatMap(List::parallelStream)
                .map(Invoice::getCustomerId)
                .filter(customer -> !customer.getCustomerFullName().isEmpty()
                        && !customer.getCustomerPhoneNumber().isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Customer deleteCustomerById(Long customerId) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return customerRepository.findById(customerId).map(customer -> {

            if (customer.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())){

                customerPreEmptives.detachCustomersFromObjects(customer);
                customerRepository.delete(customer);
                return customer;
            }

            boolean match = genericService.sellersByAuthUserId()
                    .stream()
                    .map(Seller::getSellerEmail)
                    .anyMatch(sellerName -> sellerName.equalsIgnoreCase(customer.getCreatedBy()));

            if (!match && !customer.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Operation not allowed",
                        "You cannot delete a customer not created by you or any of your sellers.", null);

            customerPreEmptives.detachCustomersFromObjects(customer);
            customerRepository.delete(customer);
            return customer;
        }).orElse(null);
    }

    @Override
    public Customer fetchCustomerById(Long customerId) {

        return fetchAllCustomers()
                .stream()
                .filter(customer -> customer.getCustomerId().equals(customerId))
                .collect(toSingleton());
    }

    @Override
    public BigDecimal fetchCustomerDebt(Long customerId) {

        return computeCustomerDebt(fetchCustomerById(customerId));
    }

    private BigDecimal computeCustomerDebt(Customer customer){

        return invoiceRepository
                .findAllByCustomerIdAndDebtGreaterThan(customer, BigDecimal.ZERO)
                .stream()
                .map(Invoice::getDebt)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
