package com.chrisworks.personal.inventorysystembackend.Backend.Entities.POJO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ReturnedStocks")
public class ReturnedStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long returnedStockId;

    @Temporal(TemporalType.DATE)
    @Column(name = "created-date")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "created-time")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updated-date")
    private Date updateDate = new Date();

    @NotNull(message = "Reason for return cannot be null")
    @Size(min = 3, message = "Reason for return must contain at least three characters")
    @Column(name = "reason-for-return", nullable = false)
    private String reasonForReturn;

    @NotNull(message = "stock name cannot be null")
    @Size(min = 3, message = "stock name must contain at least three characters")
    @Column(name = "stockName", nullable = false)
    private String stockName;

    @NotNull(message = "quantity returned cannot be null")
    @Column(name = "quantity-returned", nullable = false)
    private int quantityReturned;

    @NotNull(message = "invoice id cannot be null")
    @Size(min = 3, message = "invoice id must contain at least three characters")
    @Column(name = "invoice-id", nullable = false)
    private String invoiceId;

    @Column(name = "stock-returned-cost")
    private BigDecimal stockReturnedCost;

    @Column(name = "approved")
    private Boolean approved = false;

    @Column(name = "approved-by")
    private String approvedBy;

    @NotNull(message = "Stock returned must contain a customer detail")
    @OneToOne
    @JoinTable(name = "customer-returns", joinColumns = @JoinColumn(name = "returnedStockId"), inverseJoinColumns = @JoinColumn(name = "customerId"))
    private Customer customerId;
}
