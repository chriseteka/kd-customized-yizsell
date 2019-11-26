package com.chrisworks.personal.inventorysystembackend.Backend.Entities.POJO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
@Table(name = "Stocks")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long stockId;

    @Temporal(TemporalType.DATE)
    @Column(name = "createdDate")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "createdTime")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updatedDate")
    private Date updateDate = new Date();

    @NotNull(message = "stock category cannot be null")
    @Size(min = 3, message = "stock category must contain at least three characters")
    @Column(name = "stockCategory", nullable = false)
    private String stockCategory;

    @NotNull(message = "stock name cannot be null")
    @Size(min = 3, message = "stock name must contain at least three characters")
    @Column(name = "stockName", nullable = false, unique = true)
    private String stockName;

    @NotNull(message = "stock quantity purchased cannot be null")
    @Column(name = "stockQuantityPurchased", nullable = false)
    private int stockQuantityPurchased;

    @Column(name = "stockQuantitySold")
    private int stockQuantitySold = 0;

    @Column(name = "stockQuantityRemaining")
    private int stockQuantityRemaining;

    @NotNull(message = "stock purchased total price cannot be null")
    @Column(name = "stockPurchasedTotalPrice", nullable = false)
    private BigDecimal stockPurchasedTotalPrice;

    @Column(name = "stockSoldTotalPrice")
    private BigDecimal stockSoldTotalPrice = BigDecimal.ZERO;

    @Column(name = "stockRemainingTotalPrice")
    private BigDecimal stockRemainingTotalPrice;

    @NotNull(message = "selling price per stock cannot be null")
    @Column(name = "sellingPricePerStock", nullable = false)
    private BigDecimal sellingPricePerStock;

    @Column(name = "lastRestockQuantity")
    private int lastRestockQuantity;

    @Column(name = "profit")
    private BigDecimal profit = BigDecimal.ZERO;

    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "lastRestockBy")
    private String lastRestockBy;

    @Column(name = "approved")
    private Boolean approved = false;

    @Temporal(TemporalType.DATE)
    @Column(name = "approvedDate")
    private Date approvedDate;

    @Column(name = "approvedBy")
    private String approvedBy;

    @ManyToMany
    @JoinTable(name = "stockSupplier", joinColumns = @JoinColumn(name = "stockId"), inverseJoinColumns = @JoinColumn(name = "supplierId"))
    private Set<Supplier> stockPurchasedFrom = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "restockSupplier", joinColumns = @JoinColumn(name = "stockId"), inverseJoinColumns = @JoinColumn(name = "supplierId"))
    private Set<Supplier> lastRestockPurchasedFrom = new HashSet<>();
}
