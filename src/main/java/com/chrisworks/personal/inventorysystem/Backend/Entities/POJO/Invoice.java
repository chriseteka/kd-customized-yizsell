package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.PAYMENT_MODE;
import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long InvoiceId;

    @Temporal(TemporalType.DATE)
    @Column(name = "createdDate")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "createdTime")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updateDate")
    private Date updateDate = new Date();

    @Column(name = "invoiceNumber", unique = true)
    private String invoiceNumber;

    @DecimalMin(value = "0.0", inclusive = false, message = "Invoice amount must be greater than zero")
    @NotNull(message = "Invoice total amount cannot be null")
    @Column(name = "invoiceTotalAmount", nullable = false)
    private BigDecimal invoiceTotalAmount;

    @DecimalMin(value = "0.0", message = "Amount paid must be greater or equal to zero than zero")
    @NotNull(message = "Amount paid cannot be null")
    @Column(name = "amountPaid", nullable = false)
    private BigDecimal amountPaid;

    @Column(name = "debt")
    private BigDecimal debt = BigDecimal.ZERO;

    @Column(name = "discount")
    private BigDecimal discount = BigDecimal.ZERO;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinTable(name = "sellerInvoices", joinColumns = @JoinColumn(name = "sellerId"), inverseJoinColumns = @JoinColumn(name = "invoiceId"))
//    private Seller seller;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "stockSoldInInvoice", joinColumns = @JoinColumn(name = "invoiceId"), inverseJoinColumns = @JoinColumn(name = "stockSoldId"))
    private Set<StockSold> stockSold = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(name = "customersInvoices", joinColumns = @JoinColumn(name = "invoiceId"), inverseJoinColumns = @JoinColumn(name = "customerId"))
    private Customer customerId;

    @Column(name = "createdBy")
    private String createdBy;

    @Basic
    @JsonIgnore
    @NotNull(message = "Payment mode cannot be null")
    @Column(name = "paymentMode", nullable = false)
    private int paymentModeValue;

    @Transient
    private PAYMENT_MODE paymentMode;

    @PostLoad
    void fillTransient() {
        if (paymentModeValue > 0) {
            this.paymentMode = PAYMENT_MODE.of(paymentModeValue);
        }
    }

    @PrePersist
    void fillPersistent() {
        if (paymentMode != null) {
            this.paymentModeValue = paymentMode.getPayment_mode_value();
        }
    }
}
