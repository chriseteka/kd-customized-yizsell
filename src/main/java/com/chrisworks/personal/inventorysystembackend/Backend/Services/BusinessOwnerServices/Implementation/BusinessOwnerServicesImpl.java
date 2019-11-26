package com.chrisworks.personal.inventorysystembackend.Backend.Services.BusinessOwnerServices.Implementation;

import com.chrisworks.personal.inventorysystembackend.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystembackend.Backend.Repositories.*;
import com.chrisworks.personal.inventorysystembackend.Backend.Services.BusinessOwnerServices.BusinessOwnerServices;
import com.chrisworks.personal.inventorysystembackend.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
public class BusinessOwnerServicesImpl implements BusinessOwnerServices {

    private BusinessOwnerRepository businessOwnerRepository;

    private SellerRepository sellerRepository;

    private WarehouseRepository warehouseRepository;

    private ShopRepository shopRepository;

    private StockRepository stockRepository;

    private SupplierRepository supplierRepository;

    private CustomerRepository customerRepository;

    private InvoiceRepository invoiceRepository;

    private ReturnedStockRepository returnedStockRepository;

    private ExpenseRepository expenseRepository;

    private IncomeRepository incomeRepository;

    @Autowired
    public BusinessOwnerServicesImpl(BusinessOwnerRepository businessOwnerRepository, SellerRepository sellerRepository,
                                     WarehouseRepository warehouseRepository, ShopRepository shopRepository,
                                     StockRepository stockRepository, SupplierRepository supplierRepository,
                                     CustomerRepository customerRepository, InvoiceRepository invoiceRepository,
                                     ReturnedStockRepository returnedStockRepository, ExpenseRepository expenseRepository,
                                     IncomeRepository incomeRepository) {

        this.businessOwnerRepository = businessOwnerRepository;
        this.sellerRepository = sellerRepository;
        this.warehouseRepository = warehouseRepository;
        this.shopRepository = shopRepository;
        this.stockRepository = stockRepository;
        this.supplierRepository = supplierRepository;
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
        this.returnedStockRepository = returnedStockRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
    }

    @Override
    public BusinessOwner createAccount(BusinessOwner businessOwner) {

        return businessOwnerRepository.save(businessOwner);
    }

    @Override
    public Seller createSeller(Seller seller) {

        return sellerRepository.save(seller);
    }

    @Override
    public Warehouse addWarehouse(Warehouse warehouse) {

        return warehouseRepository.save(warehouse);
    }

    @Override
    public Seller updateSeller(Long sellerId, Seller sellerUpdates) {

        AtomicReference<Seller> updatedSeller = new AtomicReference<>();

        sellerRepository.findById(sellerId).ifPresent(seller -> {

            seller.setUpdateDate(new Date());
            seller.setSellerAddress(sellerUpdates.getSellerAddress());
            seller.setSellerPhoneNumber(seller.getSellerPhoneNumber());
            updatedSeller.set(sellerRepository.save(seller));
        });

        return updatedSeller.get();
    }

    @Override
    public Warehouse updateWarehouse(Long warehouseId, Warehouse warehouseUpdates) {

        AtomicReference<Warehouse> updatedWarehouse = new AtomicReference<>();

        warehouseRepository.findById(warehouseId).ifPresent(warehouse -> {

            warehouse.setUpdateDate(new Date());
            warehouse.setWarehouseAddress(warehouseUpdates.getWarehouseAddress());
            updatedWarehouse.set(warehouseRepository.save(warehouse));
        });

        return updatedWarehouse.get();
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
    public Shop addShop(Shop shop) {

        return shopRepository.save(shop);
    }

    @Override
    public Boolean approveStock(String stockName) {

        Stock unApprovedStock = stockRepository.findDistinctByStockNameAndApprovedIsFalse(stockName);

        if (null != unApprovedStock) {

            unApprovedStock.setApproved(true);
            unApprovedStock.setApprovedDate(new Date());
            unApprovedStock.setApprovedBy(AuthenticatedUserDetails.getUserFullName());

            return stockRepository.save(unApprovedStock) != null;
        }

        return false;
    }

    @Override
    public Boolean approveStockList(List<String> stockNameList) {

        AtomicReference<Boolean> stockApprovedFlag = new AtomicReference<>(false);

        stockNameList.forEach(stockName -> stockApprovedFlag.set(this.approveStock(stockName)));

        return stockApprovedFlag.get();
    }

    @Override
    public Boolean approveIncome(Long incomeId) {

        AtomicReference<Boolean> incomeApprovedFlag = new AtomicReference<>();

        incomeRepository.findById(incomeId).ifPresent(income -> {

            income.setApproved(true);
            income.setUpdateDate(new Date());
            income.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
            incomeApprovedFlag.set(incomeRepository.save(income) != null);
        });

        return incomeApprovedFlag.get();
    }

    @Override
    public Boolean approveExpense(Long expenseId) {

        AtomicReference<Boolean> expenseApprovedFlag = new AtomicReference<>();

        expenseRepository.findById(expenseId).ifPresent(expense -> {

            expense.setUpdateDate(new Date());
            expense.setApproved(true);
            expense.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
            expenseApprovedFlag.set(expenseRepository.save(expense) != null);
        });

        return expenseApprovedFlag.get();
    }

    @Override
    public Boolean approveReturn(Long returnedStockId) {

        AtomicReference<Boolean> returnApprovedFlag = new AtomicReference<>();

        returnedStockRepository.findById(returnedStockId).ifPresent(returnedStock -> {

            returnedStock.setUpdateDate(new Date());
            returnedStock.setApproved(true);
            returnedStock.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
            returnApprovedFlag.set(returnedStockRepository.save(returnedStock) != null);
        });

        return returnApprovedFlag.get();
    }

    @Override
    public Warehouse addShopToWarehouse(Long warehouseId, Shop newShop) {

        AtomicReference<Warehouse> updatedWarehouse = new AtomicReference<>();

        warehouseRepository.findById(warehouseId).ifPresent(warehouse -> {

            Set<Shop> allShops = warehouse.getShops();
            allShops.add(newShop);
            warehouse.setShops(allShops);
            warehouse.setUpdateDate(new Date());
            updatedWarehouse.set(warehouseRepository.save(warehouse));
        });
        return updatedWarehouse.get();
    }

    @Override
    public Warehouse addShopListToWarehouse(Long warehouseId, List<Shop> shopList) {

        AtomicReference<Warehouse> updatedWarehouse = new AtomicReference<>();

        warehouseRepository.findById(warehouseId).ifPresent(warehouse -> {

            Set<Shop> allShops = warehouse.getShops();
            allShops.addAll(shopList);
            warehouse.setShops(allShops);
            warehouse.setUpdateDate(new Date());
            updatedWarehouse.set(warehouseRepository.save(warehouse));
        });

        return updatedWarehouse.get();
    }

    @Override
    public Shop addSellerToShop(Long shopId, Seller seller) {

        AtomicReference<Shop> updatedShop = new AtomicReference<>();

        shopRepository.findById(shopId).ifPresent(shop -> {

            Set<Seller> allSellers = shop.getSellers();
            allSellers.add(seller);
            shop.setSellers(allSellers);
            shop.setUpdateDate(new Date());
            updatedShop.set(shopRepository.save(shop));
        });

        return updatedShop.get();
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
    public BusinessOwner updateAccount(BusinessOwner businessOwnerUpdates, Long businessOwnerId) {

        AtomicReference<BusinessOwner> updatedDetails = new AtomicReference<>();

        businessOwnerRepository.findById(businessOwnerId).ifPresent(businessOwner -> {

            businessOwner.setBusinessOwnerFullName(businessOwnerUpdates.getBusinessOwnerFullName());
            businessOwner.setBusinessOwnerPhoneNumber(businessOwnerUpdates.getBusinessOwnerPhoneNumber());
            businessOwner.setBusinessName(businessOwnerUpdates.getBusinessName());
            businessOwner.setUpdateDate(new Date());
            updatedDetails.set(businessOwnerRepository.save(businessOwner));
        });

        return updatedDetails.get();
    }

    @Override
    public Customer addCustomer(Customer customer) {

        customer.setCreatedBy(AuthenticatedUserDetails.getUserFullName());

        return customerRepository.save(customer);
    }

    @Override
    public Supplier addSupplier(Supplier supplier) {

        supplier.setCreatedBy(AuthenticatedUserDetails.getUserFullName());

        return supplierRepository.save(supplier);
    }

    @Transactional
    @Override
    public Stock addStock(Stock stock, Supplier supplier) {

        Supplier stockSupplier;

        stockSupplier = supplierRepository.findDistinctBySupplierPhoneNumber(supplier.getSupplierPhoneNumber());

        if (null == stockSupplier) stockSupplier = addSupplier(supplier);

        Stock existingStock = stockRepository.findDistinctByStockName(stock.getStockName());

        if (existingStock != null){

            return reStock(existingStock.getStockId(), stock, stockSupplier);
        }

        Set<Supplier> supplierSet = new HashSet<>();
        supplierSet.add(stockSupplier);

        //If stock is added by user with the role as admin, then tag the stock as approved first, set createdBy,
        // lastRestockBy and approvedBy to admin's name. Else set createdBy and lastRestockBy as seller name.

        stock.setStockQuantityRemaining(stock.getStockQuantityPurchased());
        stock.setStockRemainingTotalPrice(stock.getStockPurchasedTotalPrice());
        stock.setLastRestockQuantity(stock.getStockQuantityPurchased());
        stock.setStockPurchasedFrom(supplierSet);
        stock.setLastRestockPurchasedFrom(supplierSet);

        return stockRepository.save(stock);
    }

    @Override
    public Stock reStock(Long stockId, Stock newStock, Supplier supplier) {

        Supplier stockSupplier;

        stockSupplier = supplierRepository.findDistinctBySupplierPhoneNumber(supplier.getSupplierPhoneNumber());

        if (null == stockSupplier) stockSupplier = addSupplier(supplier);

        AtomicReference<Stock> reStock = new AtomicReference<>();

        Supplier finalStockSupplier = stockSupplier;

        stockRepository.findById(stockId).ifPresent(stock -> {

            Set<Supplier> allSuppliers = stock.getStockPurchasedFrom();
            allSuppliers.add(finalStockSupplier);

            //Restock function to be continued here

        });

        return null;
    }

    @Transactional
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
