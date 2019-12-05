package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long returnedStockId;

    @Temporal(TemporalType.DATE)
    @Column(name = "createdDate")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "createdTime")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updateDate")
    private Date updateDate = new Date();

    @NotNull(message = "Reason for return cannot be null")
    @Size(min = 3, message = "Reason for return must contain at least three characters")
    @Column(name = "reasonForReturn", nullable = false)
    private String reasonForReturn;

    @NotNull(message = "stock name cannot be null")
    @Size(min = 3, message = "stock name must contain at least three characters")
    @Column(name = "stockName", nullable = false)
    private String stockName;

    @Min(value = 1, message = "Quantity returned must be greater than zero")
    @NotNull(message = "quantity returned cannot be null")
    @Column(name = "quantityReturned", nullable = false)
    private int quantityReturned;

    @NotNull(message = "Invoice id cannot be null")
    @Size(min = 3, message = "invoice id must contain at least three characters")
    @Column(name = "invoiceId", nullable = false)
    private String invoiceId;

    @Column(name = "stockReturnedCost")
    private BigDecimal stockReturnedCost;

    @Column(name = "approved")
    private Boolean approved = false;

    @Column(name = "approvedBy")
    private String approvedBy;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinTable(name = "returnedSales", joinColumns = @JoinColumn(name = "shopId"), inverseJoinColumns = @JoinColumn(name = "returnedStockId"))
//    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(name = "customerReturns", joinColumns = @JoinColumn(name = "returnedStockId"), inverseJoinColumns = @JoinColumn(name = "customerId"))
    private Customer customerId;

    @Column(name = "createdBy")
    private String createdBy;
}
