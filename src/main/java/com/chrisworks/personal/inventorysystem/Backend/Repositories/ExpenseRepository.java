package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.EXPENSE_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Expense;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByCreatedDateIsBetween(Date from, Date to);

    List<Expense> findAllByCreatedBy(String createdBy);

    List<Expense> findAllByCreatedDate(Date createdDate);

    List<Expense> findAllByApprovedTrue();

    List<Expense> findAllByCreatedByAndApprovedIsFalse(String createdBy);

    List<Expense> findAllByExpenseTypeValue(int expenseTypeValue);

    List<Expense> findAllByShop(Shop shop);
}
