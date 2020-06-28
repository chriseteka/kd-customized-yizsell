package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ExchangedStocks")
public class ExchangedStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exchangedStockId;

    @Temporal(TemporalType.DATE)
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    private Date updateDate = new Date();

    @Size(min = 3, message = "stock name must contain at least three characters")
    @Column(name = "stockName", nullable = false)
    private String stockName;

    @Min(value = 1, message = "Quantity returned must be greater than zero")
    @Column(name = "quantityReturned", nullable = false)
    private int quantityReceived;

    @Size(min = 3, message = "invoice id must contain at least three characters")
    @Column(name = "invoiceId", nullable = false)
    private String invoiceId;

    @Column(name = "stockReturnedCost")
    private BigDecimal stockReceivedCost;

    @Column(name = "approved")
    private Boolean approved = false;

    @Column(name = "approvedBy")
    private String approvedBy;

    @Temporal(TemporalType.DATE)
    @Column(name = "approvedDate")
    private Date approvedDate;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "shopExchanges", joinColumns = @JoinColumn(name = "exchangedStockId"),
            inverseJoinColumns = @JoinColumn(name = "shopId"))
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "customerExchanges", joinColumns = @JoinColumn(name = "exchangedStockId"),
            inverseJoinColumns = @JoinColumn(name = "customerId"))
    private Customer customerId;

    @Column(name = "createdBy")
    private String createdBy;
}
