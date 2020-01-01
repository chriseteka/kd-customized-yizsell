package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Expense;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ExpenseRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.isDateEqual;
import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;

/**
 * @author Chris_Eteka
 * @since 11/29/2019
 * @email chriseteka@gmail.com
 */
@Service
public class ExpenseServicesImpl implements ExpenseServices {

    private final ExpenseRepository expenseRepository;

    private final GenericService genericService;

    @Autowired
    public ExpenseServicesImpl(ExpenseRepository expenseRepository, GenericService genericService) {
        this.expenseRepository = expenseRepository;
        this.genericService = genericService;
    }

    @Override
    public Expense createEntity(Expense expense) {

        if (null == expense) throw new InventoryAPIOperationException("Expense to save doesnt exist",
                "Expense entity to save was not found, review your inputs and try again", null);

        return genericService.addExpense(expense);
    }

    @Override
    public Expense updateEntity(Long expenseId, Expense expenseUpdates) {

        return expenseRepository.findById(expenseId).map(expense -> {

            if (!expense.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Operation not allowed",
                        "You cannot update an expense not created by you", null);

            expense.setUpdateDate(new Date());
            expense.setExpenseAmount(expenseUpdates.getExpenseAmount());
            expense.setExpenseDescription(expenseUpdates.getExpenseDescription());
            expense.setExpenseTypeVal(expenseUpdates.getExpenseTypeVal());

            return expenseRepository.save(expense);
        }).orElse(null);
    }

    @Override
    public Expense getSingleEntity(Long expenseId) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

            return expenseRepository.findById(expenseId)
                    .map(expense -> {

                        if (expense.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                            return expense;

                        boolean match = genericService.sellersByAuthUserId()
                                .stream()
                                .map(Seller::getSellerEmail)
                                .anyMatch(sellerName -> sellerName.equalsIgnoreCase(expense.getCreatedBy()));

                        if (match) return expense;
                        else throw new InventoryAPIOperationException("Operation not allowed",
                                "You cannot view an expense not created by you", null);
                    }).orElse(null);
        }

        return expenseRepository.findById(expenseId)
                .map(expense -> {

                    if (!expense.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                        throw new InventoryAPIOperationException("Operation not allowed",
                                "You cannot view an expense not created by you or any of your sellers", null);

                    return expense;
                }).orElse(null);
    }

    @Override
    public List<Expense> getEntityList() {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)
                || AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            return expenseRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());

        Set<Expense> expenseSet = genericService.sellersByAuthUserId()
                .stream()
                .map(Seller::getSellerEmail)
                .map(expenseRepository::findAllByCreatedBy)
                .flatMap(List::parallelStream)
                .collect(Collectors.toSet());
        expenseSet.addAll(expenseRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName()));

        return new ArrayList<>(expenseSet);
    }

    @Override
    public Expense deleteEntity(Long entityId) {

        if (AuthenticatedUserDetails.getAccount_type() == null ||
                !AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return expenseRepository.findById(entityId).map(expense -> {

            if (expense.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())){
                expenseRepository.delete(expense);
                return expense;
            }

            boolean match = genericService.sellersByAuthUserId()
                    .stream()
                    .map(Seller::getSellerEmail)
                    .anyMatch(sellerName -> sellerName.equalsIgnoreCase(expense.getCreatedBy()));

            if (match) {
                expenseRepository.delete(expense);
                return expense;
            }
            else throw new InventoryAPIOperationException("Operation not allowed",
                    "You cannot delete an expense not created by you or any of your sellers", null);
        }).orElse(null);

    }

    @Override
    public List<Expense> fetchAllApprovedExpenses() {

        return getEntityList()
                .stream()
                .filter(Expense::getApproved)
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> fetchAllUnApprovedExpensesCreatedBy(String createdBy) {

        return fetchAllUnApprovedExpense()
                .stream()
                .filter(expense -> expense.getCreatedBy().equalsIgnoreCase(createdBy))
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> fetchExpensesCreatedBy(String createdBy) {

        return getEntityList()
                .stream()
                .filter(expense -> expense.getCreatedBy().equalsIgnoreCase(createdBy))
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> fetchAllExpensesCreatedOn(Date createdOn) {

        return getEntityList()
                .stream()
                .filter(expense -> isDateEqual(expense.getCreatedDate(), createdOn))
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> fetchAllExpensesBetween(Date from, Date to) {

        return getEntityList()
                .stream()
                .filter(expense -> (expense.getCreatedDate().compareTo(from) >= 0)
                        && (to.compareTo(expense.getCreatedDate()) >= 0))
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> fetchAllExpensesByType(int expenseTypeValue) {

        return getEntityList()
                .stream()
                .filter(expense -> expense.getExpenseTypeValue() == expenseTypeValue)
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> fetchAllExpensesInShop(Long shopId) {

        if (AuthenticatedUserDetails.getAccount_type() == null
                || AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return genericService.shopByAuthUserId()
                .stream()
                .filter(shop -> shop.getShopId().equals(shopId))
                .map(expenseRepository::findAllByShop)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());
    }

    @Override
    public Expense approveExpense(Long expenseId) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        Expense expenseFound = fetchAllUnApprovedExpense()
                .stream()
                .filter(expense -> expense.getExpenseId().equals(expenseId))
                .collect(toSingleton());

        if (expenseFound == null) throw new InventoryAPIResourceNotFoundException
                ("Expense not found", "Expense with id " + expenseId + " was not found in your list of unapproved expense", null);

        expenseFound.setExpenseTypeVal(String.valueOf(expenseFound.getExpenseTypeValue()));
        expenseFound.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
        expenseFound.setApproved(true);
        expenseFound.setApprovedDate(new Date());

        return expenseRepository.save(expenseFound);
    }

    @Override
    public List<Expense> fetchAllUnApprovedExpense() {

        return getEntityList()
                .stream()
                .filter(expense -> !expense.getApproved())
                .collect(Collectors.toList());
    }
}
