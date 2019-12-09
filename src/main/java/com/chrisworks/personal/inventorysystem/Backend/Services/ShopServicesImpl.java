package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
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

    @Autowired
    public ShopServicesImpl(ShopRepository shopRepository, WarehouseRepository warehouseRepository,
                            IncomeRepository incomeRepository, GenericService genericService,
                            ExpenseRepository expenseRepository, ReturnedStockRepository returnedStockRepository) {
        this.shopRepository = shopRepository;
        this.warehouseRepository = warehouseRepository;
        this.incomeRepository = incomeRepository;
        this.genericService = genericService;
        this.expenseRepository = expenseRepository;
        this.returnedStockRepository = returnedStockRepository;
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
                .map(shopRepository::findAllByWarehouses)
                .orElse(Collections.emptyList());

    }

    @Override
    public Expense approveExpense(Long expenseId) {

        if (ACCOUNT_TYPE.SELLER.equals(AuthenticatedUserDetails.getAccount_type())) throw new InventoryAPIOperationException
                ("Operation not allowed", "Logged in user cannot perform this operation", null);

        Expense expenseFound = allUnApprovedExpense().stream()
                .filter(expense -> expense.getExpenseId().equals(expenseId))
                .collect(toSingleton());

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
                .map(Shop::getExpenses)
                .flatMap(Set::parallelStream)
                .filter(expense -> !expense.getApproved())
                .collect(Collectors.toList());
    }

    @Override
    public ReturnedStock approveReturnSales(Long returnSaleId) {

        if (ACCOUNT_TYPE.SELLER.equals(AuthenticatedUserDetails.getAccount_type())) throw new InventoryAPIOperationException
                ("Operation not allowed", "Logged in user cannot perform this operation", null);

        ReturnedStock returnedStockFound = allUnApprovedReturnSales().stream()
                .filter(returnedStock -> returnedStock.getReturnedStockId().equals(returnSaleId))
                .collect(toSingleton());

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
                .map(Shop::getReturnedSales)
                .flatMap(Set::parallelStream)
                .filter(returnedStock -> !returnedStock.getApproved())
                .collect(Collectors.toList());
    }

    @Override
    public Shop addShop(Warehouse warehouse, Shop shop) {

        Set<Warehouse> warehouseSet = new HashSet<>();
        warehouseSet.add(warehouse);
        shop.setWarehouses(warehouseSet);
        return shopRepository.save(shop);
    }

    @Override
    public Shop addSellerToShop(Shop shop, Seller seller) {

        if (null == seller) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find seller entity to save", null);

        Set<Seller> allSellers = shop.getSellers();
        allSellers.add(seller);
        shop.setSellers(allSellers);
        shop.setUpdateDate(new Date());

        return shopRepository.save(shop);
    }

    @Override
    public Shop findShopById(Long shopId) {
        return shopRepository.findById(shopId).orElse(null);
    }

    @Override
    public Income approveIncome(Long incomeId) {

        if (ACCOUNT_TYPE.SELLER.equals(AuthenticatedUserDetails.getAccount_type())) throw new InventoryAPIOperationException
                ("Operation not allowed", "Logged in user cannot perform this operation", null);

        Income incomeFound = allUnApprovedIncome().stream()
                .filter(income -> income.getIncomeId().equals(incomeId))
                .collect(toSingleton());

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
                .map(Shop::getIncome)
                .flatMap(Set::parallelStream)
                .filter(income -> !income.getApproved())
                .collect(Collectors.toList());
    }

    @Override
    public Shop addSellerListToShop(Long shopId, List<Seller> sellerList) {

        AtomicReference<Shop> updatedShop = new AtomicReference<>();

        shopRepository.findById(shopId).ifPresent(shop -> {

            Set<Seller> allSellers = shop.getSellers();
            allSellers.addAll(sellerList);
            shop.setSellers(allSellers);
            shop.setUpdateDate(new Date());
            updatedShop.set(shopRepository.save(shop));
        });

        return updatedShop.get();
    }

    @Override
    public Shop removeSellerFromShop(Long shopId, Seller seller) {

        AtomicReference<Shop> updatedShop = new AtomicReference<>();

        shopRepository.findById(shopId).ifPresent(shop -> {

            Set<Seller> sellerSet = shop.getSellers();
            sellerSet.remove(seller);
            shop.setSellers(sellerSet);
            updatedShop.set(shopRepository.save(shop));
        });

        return updatedShop.get();
    }

    @Override
    public Shop removeSellersFromShop(Long shopId, List<Seller> sellerList) {

        AtomicReference<Shop> updatedShop = new AtomicReference<>();

        shopRepository.findById(shopId).ifPresent(shop -> {

            Set<Seller> sellerSet = shop.getSellers();
            sellerSet.removeAll(sellerList);
            shop.setSellers(sellerSet);
            updatedShop.set(shopRepository.save(shop));
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
        return null;
    }
}
