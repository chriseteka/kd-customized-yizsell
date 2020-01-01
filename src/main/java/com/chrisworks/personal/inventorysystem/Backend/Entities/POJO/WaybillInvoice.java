package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Chris_Eteka
 * @since 12/24/2019
 * @email chriseteka@gmail.com
 */
//Object used to request, confirm and track stocks that move from warehouse to a shop for business owners with
    // warehouse and shops (WAREHOUSE_ATTENDANTS and SHOP_SELLERS)
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "WaybillInvoices")
public class WaybillInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long WaybillInvoiceId;

    @Temporal(TemporalType.DATE)
    @Column(name = "createdDate")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "createdTime")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updatedDate")
    private Date updateDate = new Date();

    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "issuedBy")
    private String issuedBy;

    @Column(name = "invoiceNumber", unique = true)
    private String waybillInvoiceNumber;

    @Column(name = "shippedStatus")
    private Boolean isWaybillShipped = false;

    @Column(name = "dateShipped")
    @Temporal(TemporalType.DATE)
    private Date dateShipped;

    @Column(name = "timeShipped")
    @Temporal(TemporalType.TIME)
    private Date timeShipped;

    @Column(name = "receivedStatus")
    private Boolean isWaybillReceived = false;

    @Column(name = "dateReceived")
    @Temporal(TemporalType.DATE)
    private Date dateReceived;

    @Column(name = "timeReceived")
    @Temporal(TemporalType.TIME)
    private Date timeReceived;

    @DecimalMin(value = "0.0", inclusive = false, message = "Invoice amount must be greater than zero")
    @Column(name = "invoiceTotalAmount", nullable = false)
    private BigDecimal waybillInvoiceTotalAmount;

    //Shop Seller requesting goods from a warehouse, we can get the shop this request was made from.
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "waybillFrom", joinColumns = @JoinColumn(name = "waybillInvoiceId"),
            inverseJoinColumns = @JoinColumn(name = "sellerId"))
    private Seller sellerRequesting;

    //Warehouse Attendant issuing the waybill to the requested shop, we can get the warehouse this stock was taken from.
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "waybillIssuer", joinColumns = @JoinColumn(name = "waybillInvoiceId"),
            inverseJoinColumns = @JoinColumn(name = "sellerId"))
    private Seller sellerIssuing;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "waybillStockInWaybillInvoice", joinColumns = @JoinColumn(name = "waybillInvoiceId",
            nullable = false, updatable = false), inverseJoinColumns = @JoinColumn(name = "waybillStockId"))
    private Set<WaybilledStocks> waybilledStocks = new HashSet<>();

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "fromShop", joinColumns = @JoinColumn(name = "waybillInvoiceId"),
            inverseJoinColumns = @JoinColumn(name = "shopId"))
    private Shop shop;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "toWarehouse", joinColumns = @JoinColumn(name = "waybillInvoiceId"), inverseJoinColumns = @JoinColumn(name = "warehouseId"))
    private Warehouse warehouse;
}
