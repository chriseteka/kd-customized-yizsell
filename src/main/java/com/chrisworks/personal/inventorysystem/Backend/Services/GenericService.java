package com.chrisworks.personal.inventorysystem.Backend.Services;


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

    StockCategory addStockCategory(StockCategory stockCategory);

    Stock addStock(Long warehouseId, Stock stock);

    Stock reStock(Long warehouseId, Long stockId, Stock newStock);

    Invoice sellStock(Invoice invoice);

    ReturnedStock processReturn(ReturnedStock returnedStock);

    List<ReturnedStock> processReturnList(List<ReturnedStock> returnedStock);

    Expense addExpense(Expense expense);

    Income addIncome(Income income);

    Stock changeStockSellingPriceById(Long stockId, BigDecimal newSellingPrice);

    Stock changeStockSellingPriceByWarehouseIdAndStockName(Long warehouseId, String stockName, BigDecimal newSellingPrice);

    Shop shopBySellerName(String sellerName);

    List<Warehouse> allWarehouseByAuthUserId();
}
