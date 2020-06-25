package com.chrisworks.personal.inventorysystem.Backend.Entities.DTO;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.PAYMENT_MODE;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;

/**
 * @author Chris_Eteka
 * @since 6/24/2020
 * @email chriseteka@gmail.com
 */
@Data
public class Invoice implements Serializable {

    private Long InvoiceId;
    private Date createdDate;
    private Date createdTime;
    private Date updateDate;
    private String invoiceNumber;
    private BigDecimal invoiceTotalAmount;
    private BigDecimal amountPaid;
    private BigDecimal debt;
    private BigDecimal balance;
    private BigDecimal discount;
    private String createdBy;
    private Seller seller;
    private Set<StockSold> stockSoldSet;
    private Customer customer;
    private int paymentModeValue;
    private String paymentModeVal;
    private PAYMENT_MODE paymentMode;
    private BigDecimal loyaltyDiscount;


    public Invoice(Long invoiceId, Date createdDate, Date createdTime, Date updateDate, String invoiceNumber,
                   BigDecimal invoiceTotalAmount, BigDecimal amountPaid, BigDecimal debt, BigDecimal balance,
                   BigDecimal discount, String createdBy, int paymentModeValue, String paymentModeVal,
                   PAYMENT_MODE payment_mode, BigDecimal loyaltyDiscount) {
        this.InvoiceId = invoiceId;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
        this.updateDate = updateDate;
        this.invoiceNumber = invoiceNumber;
        this.invoiceTotalAmount = invoiceTotalAmount;
        this.amountPaid = amountPaid;
        this.debt = debt;
        this.balance = balance;
        this.discount = discount;
        this.createdBy = createdBy;
        this.paymentModeValue = paymentModeValue;
        this.paymentModeVal = paymentModeVal;
        this.paymentMode = payment_mode;
        this.loyaltyDiscount = loyaltyDiscount;
    }

    public com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Invoice fromDTO() {
        com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Invoice invoiceFromDTO =
                new com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Invoice();
        invoiceFromDTO.setInvoiceId(this.getInvoiceId());
        invoiceFromDTO.setCreatedDate(this.getCreatedDate());
        invoiceFromDTO.setCreatedTime(this.getCreatedTime());
        invoiceFromDTO.setUpdateDate(this.getUpdateDate());
        invoiceFromDTO.setInvoiceNumber(this.getInvoiceNumber());
        invoiceFromDTO.setInvoiceTotalAmount(this.getInvoiceTotalAmount());
        invoiceFromDTO.setAmountPaid(this.getAmountPaid());
        invoiceFromDTO.setDebt(this.getDebt());
        invoiceFromDTO.setBalance(this.getBalance());
        invoiceFromDTO.setDiscount(this.getDiscount());
        invoiceFromDTO.setCreatedBy(this.getCreatedBy());
        invoiceFromDTO.setPaymentModeValue(this.getPaymentModeValue());
        invoiceFromDTO.setPaymentModeVal(this.getPaymentModeVal());
        invoiceFromDTO.setPaymentMode(this.paymentMode);
        invoiceFromDTO.setLoyaltyDiscount(this.getLoyaltyDiscount());
        invoiceFromDTO.setStockSold(this.getStockSoldSet().stream().map(StockSold::fromDTO).collect(Collectors.toSet()));
        invoiceFromDTO.setCustomerId(Stream.of(this.getCustomer()).map(Customer::fromDTO).collect(toSingleton()));
        if (this.seller != null) invoiceFromDTO.setSeller(Stream.of(this.getSeller()).map(Seller::fromDTO).collect(toSingleton()));

        return invoiceFromDTO;
    }
}
