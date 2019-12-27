package com.chrisworks.personal.inventorysystem.Backend.Services;


import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Collection;
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

    Expense addExpense(Expense expense);

    Income addIncome(Income income);

    Shop shopBySellerName(String sellerName);

    List<Warehouse> warehouseByAuthUserId();

    List<Shop> shopByAuthUserId();

    List<Supplier> fetchSuppliersByCreator(String createdBy);

    List<StockCategory> fetchAllStockCategoryByCreator(String createdBy);
}
