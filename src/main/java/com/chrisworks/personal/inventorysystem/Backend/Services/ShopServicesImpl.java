package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.*;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
@Service
public class ShopServicesImpl implements ShopServices {

    private final ShopRepository shopRepository;

    private final BusinessOwnerRepository businessOwnerRepository;

    @Autowired
    public ShopServicesImpl(ShopRepository shopRepository,
                            BusinessOwnerRepository businessOwnerRepository) {
        this.shopRepository = shopRepository;
        this.businessOwnerRepository = businessOwnerRepository;
    }

    @Override
    public Shop createShop(Long businessOwnerId, Shop shop) {

        Optional<BusinessOwner> businessOwner = businessOwnerRepository.findById(businessOwnerId);

        if (!businessOwner.isPresent()) throw new InventoryAPIOperationException
                ("Unknown user", "Could not detect the user trying to create a new shop", null);

        if (shopRepository.findDistinctByShopName(shop.getShopName()) != null) throw new InventoryAPIOperationException
                ("Shop name already exist", "A shop already exist with the name: " + shop.getShopName(), null);

        shop.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
        shop.setBusinessOwner(businessOwner.get());
        return shopRepository.save(shop);
    }

    @Override
    public Shop findShopById(Long shopId) {
        return shopRepository.findById(shopId)
                .map(shop -> {

                    if (!shop.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                        throw new InventoryAPIOperationException
                                ("shop is not yours", "This shop was not created by you", null);

                    return shop;
                })
                .orElse(null);
    }

    @Override
    public Shop updateShop(Long shopId, Shop shopUpdates) {

        AtomicReference<Shop> updatedShop = new AtomicReference<>();

        shopRepository.findById(shopId).ifPresent(shop -> {

            if (!shop.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException
                        ("shop is not yours", "This shop was not created by you", null);

            shop.setUpdateDate(shopUpdates.getUpdateDate());
            shop.setShopAddress(shopUpdates.getShopAddress());
            updatedShop.set(shopRepository.save(shop));
        });

        return updatedShop.get();
    }

    @Override
    public List<Shop> fetchAllShops() {

        return shopRepository.findAll()
                .stream()
                .filter(shop -> shop.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                .collect(Collectors.toList());
    }

    @Override
    public Shop deleteShop(Long shopId) {

        return shopRepository.findById(shopId).map(shop -> {

            if (!shop.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())) throw new
                    InventoryAPIOperationException("Not your seller", "Seller not created by you", null);

            shopRepository.delete(shop);
            return shop;
        }).orElse(null);
    }

//    @Override
//    public Expense approveExpense(Long expenseId) {
//
//        Expense expenseFound = allUnApprovedExpense().stream()
//                .filter(expense -> expense.getExpenseId().equals(expenseId))
//                .collect(toSingleton());
//
//        if (expenseFound == null) throw new InventoryAPIResourceNotFoundException
//                ("Expense not found", "Expense with id " + expenseId + " was not found in your list of unapproved expense", null);
//
//        expenseFound.setExpenseTypeVal(String.valueOf(expenseFound.getExpenseTypeValue()));
//        expenseFound.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
//        expenseFound.setApproved(true);
//        expenseFound.setApprovedDate(new Date());
//
//        return expenseRepository.save(expenseFound);
//    }

//    @Override
//    public List<Expense> allUnApprovedExpense() {
//
//        return genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(this::fetchAllShopInWarehouse)
//                .flatMap(List::parallelStream)
//                .map(expenseRepository::findAllByShop)
//                .flatMap(List::parallelStream)
//                .filter(expense -> !expense.getApproved())
//                .collect(Collectors.toList());
//    }

//    @Override
//    public ReturnedStock approveReturnSales(Long returnSaleId) {
//
//        ReturnedStock returnedStockFound = allUnApprovedReturnSales().stream()
//                .filter(returnedStock -> returnedStock.getReturnedStockId().equals(returnSaleId))
//                .collect(toSingleton());
//
//        if (returnedStockFound == null) throw new InventoryAPIResourceNotFoundException
//                ("Returned stock not found", "Returned stock with id: " + returnSaleId + "was not found in your list of" +
//                        " unapproved returned stock", null);
//
//        returnedStockFound.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
//        returnedStockFound.setApproved(true);
//        returnedStockFound.setApprovedDate(new Date());
//
//        return returnedStockRepository.save(returnedStockFound);
//    }

//    @Override
//    public List<ReturnedStock> allUnApprovedReturnSales() {
//
//        return genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(this::fetchAllShopInWarehouse)
//                .flatMap(List::parallelStream)
//                .map(returnedStockRepository::findAllByShop)
//                .flatMap(List::parallelStream)
//                .filter(returnedStock -> !returnedStock.getApproved())
//                .collect(Collectors.toList());
//    }

//    @Override
//    public Income approveIncome(Long incomeId) {
//
//        Income incomeFound = allUnApprovedIncome().stream()
//                .filter(income -> income.getIncomeId().equals(incomeId))
//                .collect(toSingleton());
//
//        if (incomeFound == null) throw new InventoryAPIResourceNotFoundException
//                ("Income not found", "Income with id: " + incomeId + " was not found in your list of unapproved income", null);
//
//        incomeFound.setIncomeTypeVal(String.valueOf(incomeFound.getIncomeTypeValue()));
//        incomeFound.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
//        incomeFound.setApproved(true);
//        incomeFound.setApprovedDate(new Date());
//
//        return incomeRepository.save(incomeFound);
//    }

//    @Override
//    public List<Income> allUnApprovedIncome() {
//
//        return genericService.warehouseByAuthUserId()
//                .stream()
//                .map(Warehouse::getWarehouseId)
//                .map(this::fetchAllShopInWarehouse)
//                .flatMap(List::parallelStream)
//                .map(incomeRepository::findAllByShop)
//                .flatMap(List::parallelStream)
//                .filter(income -> !income.getApproved())
//                .collect(Collectors.toList());
//    }
}
