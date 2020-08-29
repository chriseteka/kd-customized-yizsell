package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Chris_Eteka
 * @since 12/24/2019
 * @email chriseteka@gmail.com
 */
//This object holds stocks moved from a warehouse to a shop
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "WaybilledStocks")
public class WaybilledStocks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long WaybilledStocksId;

    @Temporal(TemporalType.DATE)
    @Column(name = "createdDate")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "createdTime")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updatedDate")
    private Date updateDate = new Date();

    @Size(min = 3, message = "stock name should have at least three characters")
    @Column(name = "stockName", nullable = false)
    private String stockName;

    @Size(min = 3, message = "stock category should have at least three characters")
    @Column(name = "stockCategory", nullable = false)
    private String stockCategory;

    @Min(value = 1, message = "Quantity sold must be greater than zero")
    @Column(name = "quantitySold", nullable = false)
    private int quantityWaybilled;

    @Column(name = "sellingPrice", nullable = false)
    private BigDecimal sellingPricePerStock;

    @Column(name = "purchasePrice", nullable = false)
    private BigDecimal purchasePricePerStock;

    @Column(name = "stockSoldInvoiceId")
    private String stockWaybillInvoiceId;

    @Column(name = "expiryDate")
    @Temporal(TemporalType.DATE)
    private Date expiryDate;

    @Column(name = "stockBarCodeId")
    private String stockBarCodeId;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "waybillStockSupplier", joinColumns = @JoinColumn(name = "waybillstockId"),
            inverseJoinColumns = @JoinColumn(name = "supplierId"))
    private Supplier stockSupplier;

    @PreUpdate
    void changeUpdatedDate(){
        this.setUpdateDate(new Date());
    }


    public WaybilledStocks(String stockName, String categoryName, int quantity, BigDecimal sellingPrice,
                           BigDecimal purchasePrice, Date expiryDate, String stockBarCodeId, Supplier stockSupplier) {
        this.stockName = stockName;
        this.stockCategory = categoryName;
        this.quantityWaybilled = quantity;
        this.sellingPricePerStock = sellingPrice;
        this.purchasePricePerStock = purchasePrice;
        this.expiryDate = expiryDate;
        this.stockBarCodeId = stockBarCodeId;
        this.stockSupplier = stockSupplier;
    }
}
