package com.chrisworks.personal.inventorysystem.Backend.Entities;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 8/27/2020
 * @email chriseteka@gmail.com
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DesktopPushObject {

    private List<ShopStocks> shopStocks;
    private List<Customer> customers;
    private List<Expense> expenses;
    private List<Income> incomes;
    private List<Invoice> invoices;
    private List<ReturnedStock> returnedStocks;
    private Long shopId;
}
