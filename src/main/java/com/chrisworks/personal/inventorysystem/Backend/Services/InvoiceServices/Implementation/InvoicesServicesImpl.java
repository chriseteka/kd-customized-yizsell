package com.chrisworks.personal.inventorysystem.Backend.Services.InvoiceServices.Implementation;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.INCOME_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.PAYMENT_MODE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Income;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Invoice;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.IncomeRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.InvoiceRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ShopRepository;
import com.chrisworks.personal.inventorysystem.Backend.Services.InvoiceServices.InvoiceServices;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
@Service
public class InvoicesServicesImpl implements InvoiceServices {

    private final InvoiceRepository invoiceRepository;

    private final ShopRepository shopRepository;

    private final IncomeRepository incomeRepository;

    @Autowired
    public InvoicesServicesImpl(InvoiceRepository invoiceRepository, ShopRepository shopRepository,
                                IncomeRepository incomeRepository) {
        this.invoiceRepository = invoiceRepository;
        this.shopRepository = shopRepository;
        this.incomeRepository = incomeRepository;
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

        if(null == entityId){

            //Throw error
            return null;
        }

        return invoiceRepository.findById(entityId).orElse(null);
    }

    @Override
    public List<Invoice> getEntityList() {

        return invoiceRepository.findAll();
    }

    @Override
    public Invoice deleteEntity(Long entityId) {

        AtomicReference<Invoice> invoiceToDelete = new AtomicReference<>(null);

        invoiceRepository.findById(entityId).ifPresent(invoice -> {

            invoiceToDelete.set(invoice);
            invoiceRepository.delete(invoice);
        });

        return invoiceToDelete.get();
    }

    @Transactional
    @Override
    public Invoice clearDebt(String invoiceNumber, BigDecimal amount) {

        Invoice invoiceFound = invoiceRepository
                .findDistinctByInvoiceNumberAndDebtGreaterThan(invoiceNumber, BigDecimal.ONE);

        if (null == invoiceFound){

            //Throw invoice not found error
            return null;
        }

        Income incomeOnDebtClearance = new Income(amount,200,"Debt cleared on invoice with id" + invoiceNumber);
        incomeOnDebtClearance.setCreatedBy(AuthenticatedUserDetails.getUserFullName());

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

            incomeOnDebtClearance.setApproved(true);
            incomeOnDebtClearance.setApprovedDate(new Date());
            incomeOnDebtClearance.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
        }

        invoiceFound.setUpdateDate(new Date());
        invoiceFound.setAmountPaid(invoiceFound.getAmountPaid().add(amount));

        incomeRepository.save(incomeOnDebtClearance);

        return invoiceRepository.save(invoiceFound);
    }

    @Override
    public List<Invoice> fetchAllInvoicesCreatedBy(String createdBy) {

        return invoiceRepository.findAllByCreatedBy(createdBy);
    }

    @Override
    public List<Invoice> fetchAllInvoiceCreatedOn(Date createdOn) {

        return invoiceRepository.findAllByCreatedDate(createdOn);
    }

    @Override
    public List<Invoice> fetchAllInvoiceCreatedBetween(Date from, Date to) {

        return invoiceRepository.findAllByCreatedDateIsBetween(from, to);
    }

    @Override
    public List<Invoice> fetchAllInvoiceInShop(Long shopId) {

        return shopRepository
                .findById(shopId)
                .map(shop -> shop.getSellers()
                .stream()
                .flatMap(seller -> seller.getInvoices().stream())
                        .collect(Collectors.toList())).orElse(null);
    }

    @Override
    public List<Invoice> fetchAllInvoiceWithDebt() {

        return invoiceRepository.findAllByDebtGreaterThan(BigDecimal.ONE);
    }

    @Override
    public List<Invoice> fetchAllInvoiceByPaymentMode(int paymentModeValue) {

        return invoiceRepository.findAllByPaymentModeValue(paymentModeValue);
    }
}
