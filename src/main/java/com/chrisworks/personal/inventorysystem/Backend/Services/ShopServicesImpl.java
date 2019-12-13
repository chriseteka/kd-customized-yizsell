package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.*;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
@Service
public class ShopServicesImpl implements ShopServices {

    private final ShopRepository shopRepository;

    private final IncomeRepository incomeRepository;

    private final GenericService genericService;

    private final WarehouseRepository warehouseRepository;

    private final ExpenseRepository expenseRepository;

    private final ReturnedStockRepository returnedStockRepository;

    private final SellerRepository sellerRepository;

    @Autowired
    public ShopServicesImpl(ShopRepository shopRepository, WarehouseRepository warehouseRepository,
                            IncomeRepository incomeRepository, GenericService genericService,
                            ExpenseRepository expenseRepository, ReturnedStockRepository returnedStockRepository,
                            SellerRepository sellerRepository) {
        this.shopRepository = shopRepository;
        this.warehouseRepository = warehouseRepository;
        this.incomeRepository = incomeRepository;
        this.genericService = genericService;
        this.expenseRepository = expenseRepository;
        this.returnedStockRepository = returnedStockRepository;
        this.sellerRepository = sellerRepository;
    }

    @Override
    public Shop updateShop(Long shopId, Shop shopUpdates) {

        AtomicReference<Shop> updatedShop = new AtomicReference<>();

        shopRepository.findById(shopId).ifPresent(shop -> {

            shop.setUpdateDate(shopUpdates.getUpdateDate());
            shop.setShopAddress(shopUpdates.getShopAddress());
            updatedShop.set(shopRepository.save(shop));
        });

        return updatedShop.get();
    }

    @Override
    public List<Shop> fetchAllShopInWarehouse(Long warehouseId) {

        if (null == warehouseId || warehouseId < 0 || !warehouseId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("warehouse id error", "warehouse id is empty or not a valid number", null);

        return warehouseRepository.findById(warehouseId)
                .map(shopRepository::findAllByWarehouse)
                .orElse(Collections.emptyList());

    }

    @Override
    public Expense approveExpense(Long expenseId) {

        Expense expenseFound = allUnApprovedExpense().stream()
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
    public List<Expense> allUnApprovedExpense() {

        return genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getWarehouseId)
                .map(this::fetchAllShopInWarehouse)
                .flatMap(List::parallelStream)
                .map(expenseRepository::findAllByShop)
                .flatMap(List::parallelStream)
                .filter(expense -> !expense.getApproved())
                .collect(Collectors.toList());
    }

    @Override
    public ReturnedStock approveReturnSales(Long returnSaleId) {

        ReturnedStock returnedStockFound = allUnApprovedReturnSales().stream()
                .filter(returnedStock -> returnedStock.getReturnedStockId().equals(returnSaleId))
                .collect(toSingleton());

        if (returnedStockFound == null) throw new InventoryAPIResourceNotFoundException
                ("Returned stock not found", "Returned stock with id: " + returnSaleId + "was not found in your list of" +
                        " unapproved returned stock", null);

        returnedStockFound.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
        returnedStockFound.setApproved(true);
        returnedStockFound.setApprovedDate(new Date());

        return returnedStockRepository.save(returnedStockFound);
    }

    @Override
    public List<ReturnedStock> allUnApprovedReturnSales() {

        return genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getWarehouseId)
                .map(this::fetchAllShopInWarehouse)
                .flatMap(List::parallelStream)
                .map(returnedStockRepository::findAllByShop)
                .flatMap(List::parallelStream)
                .filter(returnedStock -> !returnedStock.getApproved())
                .collect(Collectors.toList());
    }

    @Override
    public Shop addShop(Warehouse warehouse, Shop shop) {

        if (shopRepository.findDistinctByShopName(shop.getShopName()) != null) throw new InventoryAPIOperationException
                ("Shop name already exist", "A shop already exist with the name: " + shop.getShopName(), null);

        shop.setWarehouse(warehouse);
        return shopRepository.save(shop);
    }

    @Override
    public Shop addSellerToShop(Shop shop, Seller seller) {

        if (null == seller) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find seller entity to save", null);

        seller.setShop(shop);
        sellerRepository.save(seller);

        return shopRepository.save(shop);
    }

    @Override
    public Shop findShopById(Long shopId) {
        return shopRepository.findById(shopId).orElse(null);
    }

    @Override
    public Income approveIncome(Long incomeId) {

        Income incomeFound = allUnApprovedIncome().stream()
                .filter(income -> income.getIncomeId().equals(incomeId))
                .collect(toSingleton());

        if (incomeFound == null) throw new InventoryAPIResourceNotFoundException
                ("Income not found", "Income with id: " + incomeId + " was not found in your list of unapproved income", null);

        incomeFound.setIncomeTypeVal(String.valueOf(incomeFound.getIncomeTypeValue()));
        incomeFound.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
        incomeFound.setApproved(true);
        incomeFound.setApprovedDate(new Date());

        return incomeRepository.save(incomeFound);
    }

    @Override
    public List<Income> allUnApprovedIncome() {

        return genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getWarehouseId)
                .map(this::fetchAllShopInWarehouse)
                .flatMap(List::parallelStream)
                .map(incomeRepository::findAllByShop)
                .flatMap(List::parallelStream)
                .filter(income -> !income.getApproved())
                .collect(Collectors.toList());
    }

    @Override
    public Shop addSellerListToShop(Long shopId, List<Seller> sellerList) {

        AtomicReference<Shop> updatedShop = new AtomicReference<>();

        shopRepository.findById(shopId).ifPresent(shop -> {

            updatedShop.set(shop);
            sellerList.forEach(seller -> {

                seller.setShop(shop);
                sellerRepository.save(seller);
            });
        });

        return updatedShop.get();
    }

    @Override
    public Shop removeSellerFromShop(Long shopId, Seller seller) {

        AtomicReference<Shop> updatedShop = new AtomicReference<>();

        shopRepository.findById(shopId).ifPresent(shop -> {

            Seller sellerRetrieved = sellerRepository.findAllByShop(shop)
                    .stream()
                    .filter(sellerFoundInShop -> sellerFoundInShop.equals(seller))
                    .collect(toSingleton());

            sellerRetrieved.setShop(null);
            sellerRepository.save(sellerRetrieved);
        });

        return updatedShop.get();
    }

    @Override
    public Shop removeSellersFromShop(Long shopId, List<Seller> sellerList) {

        AtomicReference<Shop> updatedShop = new AtomicReference<>();

        shopRepository.findById(shopId).ifPresent(shop -> {

            sellerRepository.findAllByShop(shop)
                    .stream()
                    .filter(sellerFoundInShop -> sellerList.contains(sellerFoundInShop))
                    .forEach(seller -> {
                        seller.setShop(null);
                        sellerRepository.save(seller);
                    });
        });

        return updatedShop.get();
    }

    @Override
    public Shop createEntity(Shop shop) {

//        addShop(shop);
        return null;
    }

    @Override
    public Shop updateEntity(Long entityId, Shop shop) {
        return null;
    }

    @Override
    public Shop getSingleEntity(Long entityId) {
        return null;
    }

    @Override
    public List<Shop> getEntityList() {
        return null;
    }

    @Override
    public Shop deleteEntity(Long entityId) {

        AtomicReference<Shop> shopDeleted = new AtomicReference<>(null);

        shopRepository.findById(entityId).ifPresent(shop -> {

            shopDeleted.set(shop);
            shopRepository.delete(shop);
        });

        return shopDeleted.get();
    }
}
