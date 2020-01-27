package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.SalesDiscount;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockSold;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SalesDiscountRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Constants.OVER_SALE_DISCOUNT;
import static com.chrisworks.personal.inventorysystem.Backend.Utility.Constants.UNDER_SALE_DISCOUNT;
import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.isDateEqual;
import static ir.cafebabe.math.utils.BigDecimalUtils.is;

@Service
public class SalesDiscountServiceImpl implements SalesDiscountServices {

    private final SalesDiscountRepository salesDiscountRepository;

    private final SellerRepository sellerRepository;

    @Autowired
    public SalesDiscountServiceImpl(SalesDiscountRepository salesDiscountRepository, SellerRepository sellerRepository) {
        this.salesDiscountRepository = salesDiscountRepository;
        this.sellerRepository = sellerRepository;
    }

    @Override
    public void generateDiscountOnStockSold(StockSold stockSold, BigDecimal basePrice, String invoiceNumber, String customer) {

        SalesDiscount salesDiscount = new SalesDiscount();

        if (is(stockSold.getPricePerStockSold()).gt(basePrice)){

            //Creates a positive discount
            salesDiscount.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
            salesDiscount.setDiscountAmount(stockSold.getPricePerStockSold().subtract(basePrice).abs());
            salesDiscount.setDiscountType(OVER_SALE_DISCOUNT);
            salesDiscount.setInvoiceNumber(invoiceNumber);
            if (customer != null) salesDiscount.setDiscountIssuedTo(customer);
            salesDiscount.setDiscountPercentage((salesDiscount.getDiscountAmount()
                    .divide(basePrice, 2).doubleValue() * 100));
            salesDiscount.setReasonForDiscount("Positive discount on " + stockSold.getStockName()
                    + " sold with invoice number: " + salesDiscount.getInvoiceNumber());

            salesDiscountRepository.save(salesDiscount);
        }
        else if (is(stockSold.getPricePerStockSold()).lt(basePrice)){

            //Creates a negative discount
            salesDiscount.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
            salesDiscount.setDiscountAmount(basePrice.subtract(stockSold.getPricePerStockSold()).abs());
            salesDiscount.setDiscountType(UNDER_SALE_DISCOUNT);
            salesDiscount.setInvoiceNumber(invoiceNumber);
            if (customer != null) salesDiscount.setDiscountIssuedTo(customer);
            salesDiscount.setDiscountPercentage((salesDiscount.getDiscountAmount()
                    .divide(basePrice, 2).doubleValue() * 100));
            salesDiscount.setReasonForDiscount("Negative discount on " + stockSold.getStockName()
                    + " sold with invoice number: " + salesDiscount.getInvoiceNumber());

            salesDiscountRepository.save(salesDiscount);
        }
    }

    @Override
    public void generateDiscountOnInvoice(String invoiceNumber, BigDecimal discount) {
    }

    @Override
    public void generateDiscountOnLoyalCustomers(Customer customer, BigDecimal discount) {
    }

    @Override
    public List<SalesDiscount> fetchAllSalesDiscount() {

        preAuthorize();

        List<Seller> sellers = sellerRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());

        List<String> sellerEmails = sellers.stream()
                .map(Seller::getSellerEmail)
                .collect(Collectors.toList());
        sellerEmails.add(AuthenticatedUserDetails.getUserFullName());

        return sellerEmails.stream()
                .map(salesDiscountRepository::findAllByCreatedBy)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());
    }

    @Override
    public List<SalesDiscount> fetchAllSalesDiscountByType(String discountType) {

        return fetchAllSalesDiscount()
                .stream()
                .filter(salesDiscount -> salesDiscount.getDiscountType().equalsIgnoreCase(discountType))
                .collect(Collectors.toList());
    }

    @Override
    public List<SalesDiscount> fetchAllSalesDiscountByDate(Date date) {

        return fetchAllSalesDiscount()
                .stream()
                .filter(salesDiscount -> isDateEqual(salesDiscount.getCreatedDate(), date))
                .collect(Collectors.toList());
    }

    @Override
    public List<SalesDiscount> fetchAllSalesDiscountByInvoice(String invoiceNumber) {

        return fetchAllSalesDiscount()
                .stream()
                .filter(salesDiscount -> salesDiscount.getInvoiceNumber().equalsIgnoreCase(invoiceNumber))
                .collect(Collectors.toList());
    }

    @Override
    public List<SalesDiscount> fetchAllBySellerEmail(String createdBy) {

        return fetchAllSalesDiscount()
                .stream()
                .filter(salesDiscount -> salesDiscount.getCreatedBy().equalsIgnoreCase(createdBy))
                .collect(Collectors.toList());
    }

    private void preAuthorize(){

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);
    }
}