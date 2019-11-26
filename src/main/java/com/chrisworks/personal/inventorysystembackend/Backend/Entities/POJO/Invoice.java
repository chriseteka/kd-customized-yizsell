package com.chrisworks.personal.inventorysystembackend.Backend.Entities.POJO;

import com.chrisworks.personal.inventorysystembackend.Backend.Entities.ENUM.PAYMENT_MODE;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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

    @NotNull(message = "Invoice number cannot be null")
    @Column(name = "invoiceNumber", nullable = false, unique = true)
    private String invoiceNumber;

    @NotNull(message = "Payment mode cannot be null")
    @Column(name = "paymentMode", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private PAYMENT_MODE paymentMode;

    @NotNull(message = "Invoice total amount cannot be null")
    @Column(name = "invoiceTotalAmount", nullable = false)
    private BigDecimal invoiceTotalAmount;

    @Column(name = "amountPaid")
    private BigDecimal amountPaid;

    @Column(name = "debt")
    private BigDecimal debt = BigDecimal.ZERO;

    @Column(name = "discount")
    private BigDecimal discount = BigDecimal.ZERO;

    @NotNull(message = "Invoice must contain at least one stock to be sold")
    @ManyToMany
    @JoinTable(name = "stockSoldInInvoice", joinColumns = @JoinColumn(name = "invoiceId"), inverseJoinColumns = @JoinColumn(name = "stockSoldId"))
    private Set<StockSold> stockSold = new HashSet<>();

    @NotNull(message = "Invoice must contain a customer detail")
    @OneToOne
    @JoinTable(name = "customersInvoices", joinColumns = @JoinColumn(name = "invoiceId"), inverseJoinColumns = @JoinColumn(name = "customerId"))
    private Customer customerId;

    @Column(name = "createdBy")
    private String createdBy;
}
