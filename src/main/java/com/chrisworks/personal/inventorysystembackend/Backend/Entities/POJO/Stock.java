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
@Table(name = "Stocks")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long StockId;

    @Temporal(TemporalType.DATE)
    @Column(name = "created-date")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "created-time")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updated-date")
    private Date updateDate = new Date();

    @NotNull(message = "stock category cannot be null")
    @Size(min = 3, message = "stock category must contain at least three characters")
    @Column(name = "stock-category", nullable = false)
    private String stockCategory;

    @NotNull(message = "stock name cannot be null")
    @Size(min = 3, message = "stock name must contain at least three characters")
    @Column(name = "stock-name", nullable = false, unique = true)
    private String stockName;

    @NotNull(message = "stock quantity purchased cannot be null")
    @Column(name = "stock-quantity-purchased", nullable = false)
    private int stockQuantityPurchased;

    @Column(name = "stock-quantity-sold")
    private int stockQuantitySold;

    @Column(name = "stock-quantity-remaining")
    private int stockQuantityRemaining;

    @NotNull(message = "stock purchased total price cannot be null")
    @Column(name = "stock-purchased-total-price", nullable = false)
    private BigDecimal stockPurchasedTotalPrice;

    @Column(name = "stock-sold-total-price")
    private BigDecimal stockSoldTotalPrice;

    @Column(name = "stock-remaining-total-price")
    private BigDecimal stockRemainingTotalPrice;

    @NotNull(message = "selling price per stock cannot be null")
    @Column(name = "selling-price-per-stock", nullable = false)
    private BigDecimal sellingPricePerStock;

    @Column(name = "last-restock-quantity")
    private int lastRestockQuantity;

    @Column(name = "profit")
    private BigDecimal profit = BigDecimal.ZERO;

    @Column(name = "created-by")
    private String createdBy;

    @Column(name = "last-restock-by")
    private String lastRestockBy;

    @Column(name = "approved")
    private Boolean approved = false;

    @Temporal(TemporalType.DATE)
    @Column(name = "approved-date")
    private Date approvedDate;

    @Column(name = "approved-by")
    private String approvedBy;

    @NotNull(message = "Supplier details cannot be null")
    @Column(name = "stock-purchased-from", nullable = false)
    private Supplier stockPurchasedFrom;

    @Column(name = "last-restock-purchased-from")
    private Supplier lastRestockPurchasedFrom;
}
