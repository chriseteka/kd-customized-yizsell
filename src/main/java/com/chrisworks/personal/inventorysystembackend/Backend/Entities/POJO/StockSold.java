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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long stockSoldId;

    @Temporal(TemporalType.DATE)
    @Column(name = "createdDate")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "createdTime")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updatedDate")
    private Date updateDate = new Date();

    @NotNull(message = "Name of stock sold cannot be null")
    @Size(min = 3, message = "stock name should have at least three characters")
    @Column(name = "stockName", nullable = false)
    private String stockName;

    @NotNull(message = "Category of stock sold cannot be null")
    @Size(min = 3, message = "stock category should have at least three characters")
    @Column(name = "stockCategory", nullable = false)
    private String stockCategory;

    @NotNull(message = "stock sold quantity cannot be null")
    @Column(name = "quantitySold", nullable = false)
    private int quantitySold;

    @NotNull(message = "price per stock sold cannot be null")
    @Column(name = "pricePerStockSold", nullable = false)
    private BigDecimal pricePerStockSold;

    @Column(name = "stockSoldBy")
    private String stockSoldBy;
}
