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
@Table(name = "StockSold")
public class StockSold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stockSoldId;

    @Temporal(TemporalType.DATE)
    @Column(name = "created-date")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "created-time")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updated-date")
    private Date updateDate = new Date();

    @NotNull(message = "Name of stock sold cannot be null")
    @Size(min = 3, message = "stock name should have at least three characters")
    @Column(name = "stock-name", nullable = false)
    private String stockName;

    @NotNull(message = "Category of stock sold cannot be null")
    @Size(min = 3, message = "stock category should have at least three characters")
    @Column(name = "stock-category", nullable = false)
    private String stockCategory;

    @NotNull(message = "stock sold quantity cannot be null")
    @Column(name = "quantity-sold", nullable = false)
    private int quantitySold;

    @NotNull(message = "price per stock sold cannot be null")
    @Column(name = "price-per-stock-sold", nullable = false)
    private BigDecimal pricePerStockSold;

    @NotNull(message = "Seller name cannot be null")
    @Column(name = "stock-sold-by", nullable = false)
    private String stockSoldBy;
}
