package com.chrisworks.personal.inventorysystem.Backend.Services.GenericServices;


import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;

import java.math.BigDecimal;
import java.util.List;


/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
public interface GenericService {

    Customer addCustomer(Customer customer);

    Supplier addSupplier(Supplier supplier);

    Stock addStock(Stock stock, Supplier supplier);

    Stock reStock(Long stockId, Stock newStock, Supplier supplier);

    Invoice sellStock(Invoice invoice);

    ReturnedStock processReturn(ReturnedStock returnedStock);

    ReturnedStock processReturnList(List<ReturnedStock> returnedStock);

    Expense addExpense(Expense expense);

    Income addIncome(Income income);

    Stock changeStockSellingPriceById(Long stockId, BigDecimal newSellingPrice);

    Stock changeStockSellingPriceByName(String stockName, BigDecimal newSellingPrice);
}
