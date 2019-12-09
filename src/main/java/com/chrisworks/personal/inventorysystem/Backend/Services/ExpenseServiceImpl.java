package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Expense;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ExpenseRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ShopRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Chris_Eteka
 * @since 11/29/2019
 * @email chriseteka@gmail.com
 */
@Service
public class ExpenseServiceImpl implements ExpenseServices {

    private final ExpenseRepository expenseRepository;

    private ShopRepository shopRepository;

    @Autowired
    public ExpenseServiceImpl(ExpenseRepository expenseRepository, ShopRepository shopRepository) {
        this.expenseRepository = expenseRepository;
        this.shopRepository = shopRepository;
    }

    @Override
    public Expense createEntity(Expense expense) {

        if (null == expense) {
            // throw error that entity must not be null
            return null;
        }

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            expense.setApproved(true);
            expense.setApprovedDate(new Date());
            expense.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
        }


        return expenseRepository.save(expense);
    }

    @Override
    public Expense updateEntity(Long entityId, Expense expenseUpdates) {

        AtomicReference<Expense> updatedExpense = new AtomicReference<>();

        if(null == entityId){

            //Throw an error
            return null;
        }

        expenseRepository.findById(entityId).ifPresent(expense -> {

            expense.setExpenseAmount(expenseUpdates.getExpenseAmount() != null ?
                    expenseUpdates.getExpenseAmount() : expense.getExpenseAmount());
            expense.setExpenseType(expenseUpdates.getExpenseType() != null ?
                    expenseUpdates.getExpenseType() : expense.getExpenseType());
            expense.setExpenseDescription(expenseUpdates.getExpenseDescription() != null ?
                    expenseUpdates.getExpenseDescription() : expense.getExpenseDescription());

            updatedExpense.set(expenseRepository.save(expense));
        });

        return updatedExpense.get();
    }

    @Override
    public Expense getSingleEntity(Long entityId) {

        if(null == entityId){

            //Throw error
            return null;
        }

        return expenseRepository.findById(entityId).orElse(null);
    }

    @Override
    public List<Expense> getEntityList() {

        return expenseRepository.findAll();
    }

    @Override
    public Expense deleteEntity(Long entityId) {

        AtomicReference<Expense> expenseToDelete = new AtomicReference<>();

        expenseRepository.findById(entityId).ifPresent(income -> {

            expenseToDelete.set(income);
            expenseRepository.delete(income);
        });

        return expenseToDelete.get();
    }

    @Override
    public List<Expense> fetchAllApprovedExpenses() {

        return expenseRepository.findAllByApprovedTrue();
    }

    @Override
    public List<Expense> fetchAllUnApprovedExpensesCreatedBy(String createdBy) {

        return expenseRepository.findAllByCreatedByAndApprovedByIsFalse(createdBy);
    }

    @Override
    public List<Expense> fetchExpensesCreatedBy(String createdBy) {

        return expenseRepository.findAllByCreatedBy(createdBy);
    }

    @Override
    public List<Expense> fetchAllExpensesCreatedOn(Date createdOn) {

        return expenseRepository.findAllByCreatedDate(createdOn);
    }

    @Override
    public List<Expense> fetchAllExpensesBetween(Date from, Date to) {

        return expenseRepository.findAllByCreatedDateIsBetween(from, to);
    }

    @Override
    public List<Expense> fetchAllExpensesByType(int expenseTypeValue) {

        return expenseRepository.findAllByExpenseTypeValue(expenseTypeValue);
    }

    @Override
    public List<Expense> fetchAllExpensesInShop(Long shopId) {

        return shopRepository
                .findById(shopId)
                .map(shop -> new ArrayList<>(shop.getExpenses()))
                .orElse(null);
    }
}
