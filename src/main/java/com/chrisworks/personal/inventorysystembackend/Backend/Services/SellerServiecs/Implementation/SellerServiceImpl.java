package com.chrisworks.personal.inventorysystembackend.Backend.Services.SellerServiecs.Implementation;

import com.chrisworks.personal.inventorysystembackend.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystembackend.Backend.Services.SellerServiecs.SellerServices;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
public class SellerServiceImpl implements SellerServices {

    @Override
    public Seller updateAccount(Seller seller, Long userId) {
        return null;
    }

    @Override
    public Customer addCustomer(Customer customer) {
        return null;
    }

    @Override
    public Supplier addSupplier(Supplier supplier) {
        return null;
    }

    @Override
    public Stock addStock(Stock stock, Supplier supplier) {
        return null;
    }

    @Override
    public Stock reStock(Long stockId, Stock newStock, Supplier supplier) {
        return null;
    }

    @Override
    public Invoice sellStock(StockSold stockSold, Customer customer) {
        return null;
    }

    @Override
    public ReturnedStock processReturn(Invoice invoice, Customer customer) {
        return null;
    }

    @Override
    public Expense addExpense(Expense expense) {
        return null;
    }

    @Override
    public Income addIncome(Income income) {
        return null;
    }
}
