package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Expense;
import com.chrisworks.personal.inventorysystem.Backend.Services.CRUDServices;

import java.util.Date;
import java.util.List;

public interface ExpenseServices extends CRUDServices<Expense> {

    List<Expense> fetchAllApprovedExpenses();

    List<Expense> fetchAllUnApprovedExpenses();

    List<Expense> fetchExpensesCreatedBy(String createdBy);

    List<Expense> fetchAllExpensesCreatedOn(Date createdOn);

    List<Expense> fetchAllExpensesBetween(Date from, Date to);

    List<Expense> fetchAllExpensesByType(int expenseTypeValue);

    List<Expense> fetchAllExpensesInShop(Long shopId);
}
