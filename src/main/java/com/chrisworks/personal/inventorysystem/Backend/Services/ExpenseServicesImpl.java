package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Expense;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ExpenseRepository;
import com.chrisworks.personal.inventorysystem.Backend.Services.CacheManager.Interfaces.CacheInterface;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.*;
import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.getGSon;

/**
 * @author Chris_Eteka
 * @since 11/29/2019
 * @email chriseteka@gmail.com
 */
@Service
public class ExpenseServicesImpl implements ExpenseServices {

    private final ExpenseRepository expenseRepository;

    private final GenericService genericService;

    private final CacheInterface<com.chrisworks.personal.inventorysystem.Backend.Entities.DTO.Expense> expenseCacheManager;
    private final String REDIS_TABLE_KEY = "EXPENSE";

    @Autowired
    public ExpenseServicesImpl(ExpenseRepository expenseRepository, GenericService genericService,
                               CacheInterface<com.chrisworks.personal.inventorysystem.Backend.Entities.DTO.Expense> expenseCacheManager) {
        this.expenseRepository = expenseRepository;
        this.genericService = genericService;
        this.expenseCacheManager = expenseCacheManager;
    }

    @Override
    public Expense createEntity(Expense expense) {

        if (null == expense) throw new InventoryAPIOperationException("Expense to save doesnt exist",
                "Expense entity to save was not found, review your inputs and try again", null);

        Expense savedExpense = genericService.addExpense(expense);
        if (expenseCacheManager.nonEmpty(REDIS_TABLE_KEY)) cacheExpense(savedExpense);

        return savedExpense;
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

            Expense updatedExpense = expenseRepository.save(expense);
            expenseCacheManager.updateCacheDetail(REDIS_TABLE_KEY, updatedExpense.toDTO(), expenseId);

            return updatedExpense;
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

        if (expenseCacheManager.nonEmpty(REDIS_TABLE_KEY)) return fetchExpenseFromCache();

        List<Expense> expenseList;
        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)
                || AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            expenseList = expenseRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());

        else {
            expenseList = genericService.sellersByAuthUserId()
                    .stream()
                    .map(Seller::getSellerEmail)
                    .map(expenseRepository::findAllByCreatedBy)
                    .flatMap(List::parallelStream)
                    .collect(Collectors.toList());
            expenseList.addAll(expenseRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName()));
        }

        if (!expenseList.isEmpty()) expenseList.forEach(this::cacheExpense);

        return expenseList;
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
                expenseCacheManager.removeDetail(REDIS_TABLE_KEY, entityId);
                return expense;
            }

            boolean match = genericService.sellersByAuthUserId()
                    .stream()
                    .map(Seller::getSellerEmail)
                    .anyMatch(sellerName -> sellerName.equalsIgnoreCase(expense.getCreatedBy()));

            if (match) {
                expenseRepository.delete(expense);
                expenseCacheManager.removeDetail(REDIS_TABLE_KEY, entityId);
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
    public List<Expense> fetchExpensesByDescription(String description) {

        return getEntityList().stream()
            .filter(expense -> expense.getExpenseDescription().contains(description))
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
    public List<Expense> approveExpense(Long... expenseId) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        List<Expense> editedExpenseList = fetchAllUnApprovedExpense()
                .stream()
                .filter(expense -> Arrays.asList(expenseId).contains(expense.getExpenseId()))
                .peek(expenseFound -> {

                    expenseFound.setExpenseTypeVal(String.valueOf(expenseFound.getExpenseTypeValue()));
                    expenseFound.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
                    expenseFound.setApproved(true);
                    expenseFound.setApprovedDate(new Date());
                }).collect(Collectors.toList());

        List<Expense> approvedExpenseList = expenseRepository.saveAll(editedExpenseList);
        approvedExpenseList.forEach(expense -> expenseCacheManager
                .updateCacheDetail(REDIS_TABLE_KEY, expense.toDTO(), expense.getExpenseId()));

        return approvedExpenseList;
    }

    @Override
    public List<Expense> fetchAllUnApprovedExpense() {

        return getEntityList()
                .stream()
                .filter(expense -> !expense.getApproved())
                .collect(Collectors.toList());
    }

    private void cacheExpense(Expense expense){
        expenseCacheManager.cacheDetail(REDIS_TABLE_KEY, expense.toDTO(), expense.getExpenseId());
    }

    private List<Expense> fetchExpenseFromCache(){

        return expenseCacheManager.fetchDetailsByKey(REDIS_TABLE_KEY, data ->
                new ArrayList<>(getGSon().fromJson(data.stream()
                                .map(entry -> getGSon().toJson(entry.getValue()))
                                .collect(Collectors.toList()).toString(),
                        new TypeToken<ArrayList<com.chrisworks.personal.inventorysystem.Backend.Entities.DTO.Expense>>(){}.getType())))
                .stream().map(com.chrisworks.personal.inventorysystem.Backend.Entities.DTO.Expense::fromDTO)
                .collect(Collectors.toList());
    }
}
