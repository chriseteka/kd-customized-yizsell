package com.chrisworks.personal.inventorysystembackend.Backend.Services.GenericServices;


import com.chrisworks.personal.inventorysystembackend.Backend.Entities.POJO.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
@Service
@Transactional
public interface GenericService<T> {

    T updateAccount(T t, Long userId);

    Customer addCustomer(Customer customer);

    Supplier addSupplier(Supplier supplier);

    Stock addStock(Stock stock, Supplier supplier);

    Stock reStock(Long stockId, Stock newStock, Supplier supplier);

    Invoice sellStock(StockSold stockSold, Customer customer);

    ReturnedStock processReturn(Invoice invoice, Customer customer);

    Expense addExpense(Expense expense);

    Income addIncome(Income income);
}
