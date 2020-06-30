package com.chrisworks.personal.inventorysystem.Backend.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Chris_Eteka
 * @since 6/30/2020
 * @email chriseteka@gmail.com
 */
@Data
@AllArgsConstructor
public class Stat {

    private int noOfShops;
    private int noOfWarehouses;
    private int noOfStaff;
    private int noOfInvoices;
    private int noOfWaybillInvoices;
    private int noOfCustomer;
    private int noOfSuppliers;
    private int noOfShopStocks;
    private int noOfWarehouseStocks;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal totalDiscounts;
    private BigDecimal totalDebts;
    private BigDecimal profit;
}
