package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.formatDate;

/**
 * @author Chris_Eteka
 * @since 1/11/2020
 * @email chriseteka@gmail.com
 */
@Data
public class Report {

    private final String reportGeneratedDate = formatDate(new Date());

    private String title;

    private String businessName;

    private int totalWarehouses;

    private int totalShops;

    private int totalNumberOfRegisteredStaff;

    private int totalNumberOfStockCategory;

    private int totalRegisteredStockInWarehouses;

    private int totalRegisteredStockInShops;

    private int totalStockAvailableInWarehouses;

    private int totalStockAvailableInShops;

    private int totalNumberOfNewStockAddedToWarehouse;

    private int totalNumberOfNewStockAddedToShop;

    private int totalNumberOfStockSold;

    private int totalNumberOfStockReturned;

    private int totalNumberOfSalesInvoices;

    private int totalNumberOfWareBillInvoices;

    private BigDecimal totalIncurredExpenses;

    private BigDecimal totalIncomeMade;

    private BigDecimal totalDebtsRegistered;

    private BigDecimal totalEstimatedCashAtHand;
}
