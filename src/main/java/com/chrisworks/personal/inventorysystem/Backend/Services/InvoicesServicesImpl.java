package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.*;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static ir.cafebabe.math.utils.BigDecimalUtils.is;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
@Service
public class InvoicesServicesImpl implements InvoiceServices {

    private final InvoiceRepository invoiceRepository;

    private final CustomerRepository customerRepository;

    private final IncomeRepository incomeRepository;

    private final SellerRepository sellerRepository;

    private final GenericService genericService;

    @Autowired
    public InvoicesServicesImpl(InvoiceRepository invoiceRepository, CustomerRepository customerRepository,
                                IncomeRepository incomeRepository, SellerRepository sellerRepository,
                                GenericService genericService) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.incomeRepository = incomeRepository;
        this.sellerRepository = sellerRepository;
        this.genericService = genericService;
    }

    @Override
    public Invoice createEntity(Invoice invoice) {
        return null;
    }

    @Override
    public Invoice updateEntity(Long entityId, Invoice invoice) {
        return null;
    }

    @Override
    public Invoice getSingleEntity(Long entityId) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return invoiceRepository.findById(entityId)
                .map(invoice -> {

                    boolean match = genericService.shopByAuthUserId()
                            .stream()
                            .map(sellerRepository::findAllByShop)
                            .flatMap(List::parallelStream)
                            .map(Seller::getSellerEmail)
                            .anyMatch(sellerName -> sellerName.equalsIgnoreCase(invoice.getCreatedBy()));

                    if (match) return invoice;
                    else throw new InventoryAPIOperationException("Not your invoice",
                            "The invoice you are trying to retrieve was not created by any of yor sellers", null);
                }).orElse(null);
    }

    @Override
    public List<Invoice> getEntityList() {

        if (AuthenticatedUserDetails.getAccount_type() == null
                || AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user cannot perform this operation", null);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER))
            return fetchAllInvoicesCreatedBy(AuthenticatedUserDetails.getUserFullName());

        List<Invoice> invoiceList = genericService.sellersByAuthUserId()
                .stream()
                .map(invoiceRepository::findAllBySeller)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());
        invoiceList.addAll(fetchAllInvoicesCreatedBy(AuthenticatedUserDetails.getUserFullName()));

        return invoiceList;
    }

    @Override
    public Invoice deleteEntity(Long entityId) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return invoiceRepository.findById(entityId).map(invoice -> {

            if (invoice.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())){
                invoiceRepository.delete(invoice);
                return invoice;
            }

            boolean match = genericService.sellersByAuthUserId()
                    .stream()
                    .map(Seller::getSellerEmail)
                    .anyMatch(sellerName -> sellerName.equalsIgnoreCase(invoice.getCreatedBy()));

            if (match){
                invoiceRepository.delete(invoice);
                return invoice;
            }
            else throw new InventoryAPIOperationException("Not your invoice",
                    "The invoice you are trying to delete was not created by any of yor sellers", null);
        }).orElse(null);
    }

    @Transactional
    @Override
    public Invoice clearDebt(String invoiceNumber, BigDecimal amount) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        Invoice invoiceFound = invoiceRepository
                .findDistinctByInvoiceNumberAndDebtGreaterThan(invoiceNumber, BigDecimal.ONE);

        if (null == invoiceFound) throw new InventoryAPIResourceNotFoundException("Invoice not found",
                "Invoice with invoice number: " + invoiceNumber + " was not found in the list of invoices with debt," +
                        " hence you cannot proceed with the debt clearance, confirm your inputs and try again", null);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)){

            if (invoiceFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                return proceedWithDebtClearance(invoiceFound, amount);

            Shop shop = genericService.shopBySellerName(AuthenticatedUserDetails.getUserFullName());

            boolean match = sellerRepository.findAllByCreatedBy(shop.getCreatedBy())
                    .stream()
                    .map(Seller::getSellerEmail)
                    .anyMatch(sellerName -> sellerName.equalsIgnoreCase(invoiceFound.getCreatedBy()));

            if (match) return proceedWithDebtClearance(invoiceFound, amount);
            else throw new InventoryAPIOperationException("Cannot clear debt",
                    "Cannot clear debt, invoice with number: " + invoiceNumber + " was not found in this shop", null);
        }

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

            if (invoiceFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                return proceedWithDebtClearance(invoiceFound, amount);

            boolean match = genericService.sellersByAuthUserId()
                    .stream()
                    .map(Seller::getSellerEmail)
                    .anyMatch(sellerName -> sellerName.equalsIgnoreCase(invoiceFound.getCreatedBy()));

            if (match) return proceedWithDebtClearance(invoiceFound, amount);
            else throw new InventoryAPIOperationException("Cannot clear debt",
                    "Cannot clear debt, invoice was not found in any the shops created by you," +
                            " please confirm your inputs and try again", null);
        }

        throw new InventoryAPIOperationException("Unknown user", "Could not identify the user trying to make this" +
                " request, hence cannot proceed with the operation", null);
    }

    @Override
    public Invoice fetchInvoiceByInvoiceNumber(String invoiceNumber) {

            Invoice invoiceFound = invoiceRepository.findDistinctByInvoiceNumber(invoiceNumber);

            if (null == invoiceFound) throw new InventoryAPIResourceNotFoundException("Invoice not found",
                    "Invoice with invoice number: " + invoiceNumber + " was not found", null);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

            if (invoiceFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                return invoiceFound;

            boolean match = genericService.sellersByAuthUserId()
                    .stream()
                    .map(Seller::getSellerEmail)
                    .anyMatch(sellerName -> sellerName.equalsIgnoreCase(invoiceFound.getCreatedBy()));

            if (match) return invoiceFound;
            else throw new InventoryAPIOperationException("Invoice not yours",
                    "The invoice you are about to retrieve was not created by you or any of your sellers", null);
        }

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)) {

            if (invoiceFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                return invoiceFound;

            Shop shop = genericService.shopBySellerName(AuthenticatedUserDetails.getUserFullName());

            boolean match = sellerRepository.findAllByShop(shop)
                    .stream()
                    .map(Seller::getSellerEmail)
                    .anyMatch(sellerName -> sellerName.equalsIgnoreCase(invoiceFound.getCreatedBy()));

            if (match) return invoiceFound;
            else throw new InventoryAPIOperationException("Invoice not yours", "The invoice you are about to retrieve " +
                    "was not created by you or any of the sellers in this shop", null);
        }

        else throw new InventoryAPIOperationException("Invoice not yours",
                "The invoice you are about to retrieve was not created by you or any of your sellers", null);
    }

    @Override
    public List<Invoice> fetchAllInvoicesCreatedBy(String createdBy) {

        return invoiceRepository.findAllByCreatedBy(createdBy);
    }

    @Override
    public List<Invoice> fetchAllInvoiceCreatedOn(Date createdOn) {

        return getEntityList()
                .stream()
                .filter(invoice -> invoice.getCreatedDate().compareTo(createdOn) == 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> fetchAllInvoiceCreatedBetween(Date from, Date to) {

        return getEntityList()
                .stream()
                .filter(invoice -> invoice.getCreatedDate().compareTo(from) >= 0
                        && to.compareTo(invoice.getCreatedDate()) >= 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> fetchAllInvoiceInShop(Long shopId) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        List<Invoice> invoiceList = genericService.shopByAuthUserId()
                .stream()
                .map(sellerRepository::findAllByShop)
                .flatMap(List::parallelStream)
                .map(invoiceRepository::findAllBySeller)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            invoiceList.addAll(fetchAllInvoicesCreatedBy(AuthenticatedUserDetails.getUserFullName()));
            return invoiceList;
        }

        return invoiceList;
    }

    @Override
    public List<Invoice> fetchAllInvoiceWithDebt() {

        return getEntityList()
                .stream()
                .filter(invoice -> is(invoice.getDebt()).isPositive())
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> fetchAllInvoiceByPaymentMode(int paymentModeValue) {

        return getEntityList()
                .stream()
                .filter(invoice -> invoice.getPaymentModeValue() == paymentModeValue)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> fetchAllInvoicesBySeller(Long sellerId) {

        if (AuthenticatedUserDetails.getAccount_type() == null
                || !AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return genericService.sellersByAuthUserId()
                .stream()
                .filter(seller -> seller.getSellerId().equals(sellerId))
                .map(invoiceRepository::findAllBySeller)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> fetchInvoicesByCustomer(Long customerId) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return customerRepository.findById(customerId).map(customer -> {

            List<Invoice> invoiceList = invoiceRepository.findAllByCustomerId(customer);

            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
                return invoiceList
                        .stream()
                        .filter(invoice -> invoice.getCreatedBy()
                                .equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())
                                || invoice.getSeller().getCreatedBy()
                                .equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                        .collect(Collectors.toList());

            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER))
                return invoiceList
                        .stream()
                        .filter(invoice -> invoice.getSeller().getSellerEmail()
                                .equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())
                                || invoice.getSeller().getShop().equals(genericService.shopByAuthUserId().get(0)))
                        .collect(Collectors.toList());

            else throw new InventoryAPIOperationException("Customer invoices not yours",
                    "Invoices found with this customer id were not created by you or any of your sellers", null);
        }).orElse(null);
    }

    private Invoice proceedWithDebtClearance(Invoice invoiceFound, BigDecimal amount) {

        Income incomeOnDebtClearance = new Income(amount,200,
                "Debt cleared on invoice with id: " + invoiceFound.getInvoiceNumber());
        incomeOnDebtClearance.setShop(invoiceFound.getSeller().getShop());
        incomeOnDebtClearance.setCreatedBy(AuthenticatedUserDetails.getUserFullName());

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

            incomeOnDebtClearance.setApproved(true);
            incomeOnDebtClearance.setApprovedDate(new Date());
            incomeOnDebtClearance.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
        }

        invoiceFound.setPaymentModeVal(String.valueOf(invoiceFound.getPaymentModeValue()));
        invoiceFound.setUpdateDate(new Date());
        invoiceFound.setDebt(invoiceFound.getDebt().subtract(amount));
        invoiceFound.setAmountPaid(invoiceFound.getAmountPaid().add(amount));

        incomeRepository.save(incomeOnDebtClearance);

        return invoiceRepository.save(invoiceFound);
    }
}
