package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Chris_Eteka
 * @since 12/24/2019
 * @email chriseteka@gmail.com
 */
//This class holds stocks for business owners that has warehouse, stocks are added first into their warehouse
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "WarehouseStocks")
public class WarehouseStocks {

    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    @GeneratedValue(generator = "generator")
    @GenericGenerator(name = "generator", strategy = "increment")
    private Long warehouseStockId;

    @Temporal(TemporalType.DATE)
    @Column(name = "createdDate")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "createdTime")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updatedDate")
    private Date updateDate = new Date();

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(name = "warehouseStockStockCategory", joinColumns = @JoinColumn(name = "warehouseStockId"),
            inverseJoinColumns = @JoinColumn(name = "stockCategoryId"))
    private StockCategory stockCategory;

    @Size(min = 3, message = "stock name must contain at least three characters")
    @Column(name = "stockName", nullable = false)
    private String stockName;

    @Min(value = 1, message = "Stock quantity purchased must be greater than zero")
    @Column(name = "stockQuantityPurchased", nullable = false)
    private int stockQuantityPurchased;

    @Column(name = "stockQuantitySold")
    private int stockQuantitySold = 0;

    @Column(name = "stockQuantityRemaining")
    private int stockQuantityRemaining;

    @DecimalMin(value = "0.0", inclusive = false, message = "Stock purchased total price must be greater than zero")
    @Column(name = "stockPurchasedTotalPrice", nullable = false)
    private BigDecimal stockPurchasedTotalPrice;

    @Column(name = "pricePerStockPurchased")
    private BigDecimal pricePerStockPurchased;

    @Column(name = "stockSoldTotalPrice")
    private BigDecimal stockSoldTotalPrice = BigDecimal.ZERO;

    @Column(name = "stockRemainingTotalPrice")
    private BigDecimal stockRemainingTotalPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "Selling price per stock must be greater than zero")
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

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "warehouseStockSupplier", joinColumns = @JoinColumn(name = "warehouseStockId"),
            inverseJoinColumns = @JoinColumn(name = "supplierId"))
    private Set<Supplier> stockPurchasedFrom = new HashSet<>();

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinTable(name = "warehouseRestockSupplier", joinColumns = @JoinColumn(name = "warehouseStockId"),
            inverseJoinColumns = @JoinColumn(name = "supplierId"))
    private Supplier lastRestockPurchasedFrom;

    @Column(name = "expiryDate")
    @Temporal(TemporalType.DATE)
    private Date expiryDate;

    @Column(name = "stockBarCodeId")
    private String stockBarCodeId;

    @Column(name = "possibleQuantityRemaining")
    private int possibleQuantityRemaining;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinTable(name = "stocksInWarehouse", joinColumns = @JoinColumn(name = "warehouseStockId"),
            inverseJoinColumns = @JoinColumn(name = "warehouseId"))
    private Warehouse warehouse;
}
