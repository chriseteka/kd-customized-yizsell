package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
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

    @Size(min = 3, message = "stock name should have at least three characters")
    @Column(name = "stockName", nullable = false)
    private String stockName;

    @Size(min = 3, message = "stock category should have at least three characters")
    @Column(name = "stockCategory", nullable = false)
    private String stockCategory;

    @Min(value = 1, message = "Quantity sold must be greater than zero")
    @Column(name = "quantitySold", nullable = false)
    private int quantitySold;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price per stock sold must be greater than zero")
    @Column(name = "pricePerStockSold", nullable = false)
    private BigDecimal pricePerStockSold;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "costPricePerStock")
    private BigDecimal costPricePerStock;

    @Column(name = "stockSoldInvoiceId")
    private String stockSoldInvoiceId;

    @Column(name = "createdBy")
    private String createdBy;

    private boolean isSoldOnPromo = false;

    //This must be set from the front end if the application must take not of the promo applied
    @Transient
    private boolean isPromoApplied = false;
}
