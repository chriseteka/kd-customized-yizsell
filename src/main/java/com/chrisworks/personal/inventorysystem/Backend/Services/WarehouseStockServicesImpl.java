package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.WarehouseStocks;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Supplier;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockCategory;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.WarehouseRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.WarehouseStockRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.StockCategoryRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SupplierRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static ir.cafebabe.math.utils.BigDecimalUtils.is;

/**
 * @author Chris_Eteka
 * @since 12/24/2019
 * @email chriseteka@gmail.com
 */
@Service
public class WarehouseStockServicesImpl implements WarehouseStockServices {

    private final SellerRepository sellerRepository;

    private final WarehouseRepository warehouseRepository;

    private final WarehouseStockRepository warehouseStockRepository;

    private final GenericService genericService;

    private final StockCategoryRepository stockCategoryRepository;

    private final SupplierRepository supplierRepository;

    @Autowired
    public WarehouseStockServicesImpl(SellerRepository sellerRepository, WarehouseRepository warehouseRepository,
                                      WarehouseStockRepository warehouseStockRepository, GenericService genericService,
                                      StockCategoryRepository stockCategoryRepository, SupplierRepository supplierRepository) {
        this.sellerRepository = sellerRepository;
        this.warehouseRepository = warehouseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.genericService = genericService;
        this.stockCategoryRepository = stockCategoryRepository;
        this.supplierRepository = supplierRepository;
    }

    @Transactional
    @Override
    public WarehouseStocks createStockInWarehouse(Long warehouseId, WarehouseStocks stock) {

        if (AuthenticatedUserDetails.getAccount_type() == null) throw new InventoryAPIOperationException
                ("Unknown user", "Could not identify the type of user trying to add stock to warehouse", null);

        return warehouseRepository.findById(warehouseId).map(warehouse -> {

            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT)){

                return sellerRepository.findById(AuthenticatedUserDetails.getUserId())
                        .map(seller -> {

                            if (!seller.getCreatedBy().equalsIgnoreCase(warehouse.getCreatedBy())) throw new
                                    InventoryAPIOperationException
                                    ("Not your warehouse", "Warehouse does not belong to your creator", null);

                            return addStockToWarehouse(stock, warehouse);
                        }).orElse(null);
            }
            else if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)
                    && AuthenticatedUserDetails.getHasWarehouse()){

                if (!warehouse.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())) throw new
                        InventoryAPIOperationException("Not your warehouse", "Warehouse you are about to add a stock" +
                        " does not belong to you, cannot proceed with this operation", null);

                return addStockToWarehouse(stock, warehouse);
            }
            else throw new InventoryAPIOperationException
                    ("Operation not allowed", "User attempting this operation is not allowed to proceed", null);
        }).orElse(null);
    }

    @Override
    public List<WarehouseStocks> allStockByWarehouseId(Long warehouseId) {

        if (AuthenticatedUserDetails.getAccount_type() == null) throw new InventoryAPIOperationException
                ("Unknown user", "Could not identify the type of user trying to view all stock in warehouse", null);

        if(AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)) throw new
                InventoryAPIOperationException("Operation not allowed", "You are not authorized to add stocks" +
                " to a warehouse, contact the business owner or warehouse attendant", null);

        return warehouseRepository.findById(warehouseId)
                .map(warehouse -> {

                    if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT)
                            && !sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails
                            .getUserFullName()).getCreatedBy().equalsIgnoreCase(warehouse.getCreatedBy()))
                        throw new InventoryAPIOperationException
                                ("Not your warehouse", "Warehouse does not belong to your creator", null);

                    if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)
                            && !warehouse.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                        throw new InventoryAPIOperationException("Not your warehouse", "You cannot retrieve stock from" +
                                " this warehouse because it was not created by you", null);

                    return warehouseStockRepository.findAllByWarehouse(warehouse);
                }).orElse(Collections.emptyList());

    }

    @Override
    public List<WarehouseStocks> allSoonToFinishStock(Long warehouseId, int limit) {

        return this.allStockByWarehouseId(warehouseId)
                .stream()
                .filter(warehouseStocks -> warehouseStocks.getStockQuantityRemaining() <= limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<WarehouseStocks> allSoonToExpireStock(Long warehouseId, Date expiryDateInterval) {

        return this.allStockByWarehouseId(warehouseId)
                .stream()
                .filter(warehouseStocks -> expiryDateInterval.getTime()
                        - warehouseStocks.getExpiryDate().getTime() <= 60)
                .collect(Collectors.toList());
    }

    @Override
    public List<WarehouseStocks> allApprovedStock(Long warehouseId) {

        return this.allStockByWarehouseId(warehouseId)
                .stream()
                .filter(WarehouseStocks::getApproved)
                .collect(Collectors.toList());
    }

    @Override
    public List<WarehouseStocks> allUnApprovedStock(Long warehouseId) {

        return this.allStockByWarehouseId(warehouseId)
                .stream()
                .filter(warehouseStocks -> !warehouseStocks.getApproved())
                .collect(Collectors.toList());
    }

    @Override
    public List<WarehouseStocks> allUnApprovedStockByCreator(String createdBy) {

        return warehouseStockRepository.findAllByCreatedByAndApprovedIsFalse(createdBy);
    }

    @Override
    public WarehouseStocks approveStock(Long stockId) {

        if (AuthenticatedUserDetails.getAccount_type() == null) throw new InventoryAPIOperationException
                ("Unknown user", "Could not identify the type of user trying to view all stock in warehouse", null);

        if(AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)
                || AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT)) throw new
                InventoryAPIOperationException("Operation not allowed", "You are not authorized to add stocks" +
                " to a warehouse, contact the business owner or warehouse attendant", null);

        return warehouseStockRepository.findById(stockId)
                .map(stockFound -> {

                    if (!stockFound.getWarehouse().getCreatedBy()
                            .equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                        throw new InventoryAPIOperationException("Not allowed", "Stock not found in yor warehouse", null);

                    stockFound.setUpdateDate(new Date());
                    stockFound.setApproved(true);
                    stockFound.setApprovedDate(new Date());
                    stockFound.setApprovedBy(AuthenticatedUserDetails.getUserFullName());

                    return warehouseStockRepository.save(stockFound);
                }).orElse(null);
    }

    @Override
    public List<WarehouseStocks> approveStockList(List<Long> stockIdList) {

        return stockIdList.stream()
                .map(this::approveStock)
                .collect(Collectors.toList());
    }

    @Override
    public WarehouseStocks deleteStock(Long stockId) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) throw new
                InventoryAPIOperationException("Not allowed", "Operation not allowed for logged in user.", null);

        return warehouseStockRepository.findById(stockId)
                .map(stockFound -> {

                    if (!stockFound.getWarehouse().getCreatedBy()
                            .equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                        throw new InventoryAPIOperationException("Not allowed", "Stock not found in yor warehouse", null);

                    warehouseStockRepository.delete(stockFound);
                    return stockFound;
                }).orElse(null);
    }

    @Transactional
    @Override
    public WarehouseStocks reStockToWarehouse(Long warehouseId, Long stockId, WarehouseStocks newStock) {

        if(AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)) throw new
                InventoryAPIOperationException("Operation not allowed", "You are not authorized to add stocks" +
                " to a warehouse, contact the business owner or warehouse attendant", null);

        if (null == warehouseId || warehouseId < 0 || !warehouseId.toString().matches("\\d+")
                || null == stockId || stockId < 0 || !stockId.toString().matches("\\d+")) throw new InventoryAPIOperationException
                ("warehouse id or stock id error", "warehouse id and/or stock id is empty or not a valid number", null);

        if (null == newStock) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find stock entity to save", null);

        return warehouseRepository.findById(warehouseId).map(warehouse -> {

            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT)
                    && !sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails
                    .getUserFullName()).getCreatedBy().equalsIgnoreCase(warehouse.getCreatedBy()))
                throw new InventoryAPIOperationException
                        ("Not your warehouse", "Warehouse does not belong to your creator", null);

            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)
                    && !warehouse.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Not your warehouse", "You cannot add stock to" +
                        " this warehouse because it was not created by you", null);

            Supplier stockSupplier = newStock.getLastRestockPurchasedFrom();

            stockSupplier = supplierRepository.findBySupplierPhoneNumber(stockSupplier.getSupplierPhoneNumber());

            if (null == stockSupplier) stockSupplier = genericService
                    .addSupplier(newStock.getLastRestockPurchasedFrom());

            Supplier finalStockSupplier = stockSupplier;

            Optional<WarehouseStocks> optionalStock = warehouseStockRepository.findById(stockId);

            if (!optionalStock.isPresent()) throw new InventoryAPIOperationException
                    ("could not find an entity", "Could not find stock with the id " + stockId, null);

            return optionalStock.map(stock -> {

                Set<Supplier> allSuppliers = stock.getStockPurchasedFrom();
                allSuppliers.add(finalStockSupplier);
                stock.setUpdateDate(new Date());
                stock.setStockPurchasedFrom(allSuppliers);
                stock.setLastRestockPurchasedFrom(finalStockSupplier);
                stock.setLastRestockBy(AuthenticatedUserDetails.getUserFullName());
                stock.setLastRestockQuantity(newStock.getStockQuantityPurchased());
                stock.setSellingPricePerStock(newStock.getSellingPricePerStock());
                stock.setStockQuantityPurchased(newStock.getStockQuantityPurchased() + stock.getStockQuantityPurchased());
                stock.setStockQuantityRemaining(newStock.getStockQuantityPurchased() + stock.getStockQuantityRemaining());
                stock.setPossibleQuantityRemaining(newStock.getStockQuantityPurchased() + stock.getPossibleQuantityRemaining());
                stock.setStockPurchasedTotalPrice(newStock.getStockPurchasedTotalPrice().add(stock.getStockPurchasedTotalPrice()));
                stock.setStockRemainingTotalPrice(newStock.getStockPurchasedTotalPrice().add(stock.getStockRemainingTotalPrice()));
                stock.setPricePerStockPurchased(newStock.getStockPurchasedTotalPrice()
                        .divide(BigDecimal.valueOf(newStock.getStockQuantityPurchased()), 2));

                if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

                    stock.setApproved(true);
                    stock.setApprovedDate(new Date());
                    stock.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
                }
                return warehouseStockRepository.save(stock);
            }).orElse(null);
        }).orElse(null);
    }

    @Override
    public WarehouseStocks changeStockSellingPriceByStockId(Long stockId, BigDecimal newSellingPrice) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER))
            throw new InventoryAPIOperationException("Not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        if (null == stockId || stockId < 0 || !stockId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("stock id error", "stock id is empty or not a valid number", null);

        if (null == newSellingPrice || is(newSellingPrice).lte(BigDecimal.ZERO) || !newSellingPrice.toString().matches("\\d+"))
            throw new InventoryAPIOperationException("selling price error", "selling price is empty or not a valid number", null);

        AtomicReference<WarehouseStocks> updatedStock = new AtomicReference<>();

        warehouseStockRepository.findById(stockId).ifPresent(stock -> {

            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)
                    && !stock.getWarehouse().getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Not allowed",
                        "You cannot change selling price of a stock not found in your warehouse", null);

            if(AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT)
                    && !sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName()).getWarehouse()
                    .equals(stock.getWarehouse())) throw new InventoryAPIOperationException("Not allowed",
                    "You cannot change selling price of a stock not found in your warehouse", null);

            updatedStock.set(warehouseStockRepository.save(changeStockSellingPrice(stock, newSellingPrice)));
        });

        return updatedStock.get();
    }

    @Override
    public WarehouseStocks changeStockSellingPriceByWarehouseIdAndStockName(Long warehouseId, String stockName, BigDecimal newSellingPrice) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Not allowed", "Operation not allowed", null);

        if (null == warehouseId || warehouseId < 0 || !warehouseId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("warehouse id error", "warehouse id is empty or not a valid number", null);

        if (null == stockName || stockName.isEmpty()) throw new
                InventoryAPIOperationException("stock name error", "stock name is empty or null", null);

        if (null == newSellingPrice || is(newSellingPrice).lte(BigDecimal.ZERO) || !newSellingPrice.toString().matches("\\d+"))
            throw new InventoryAPIOperationException("selling price error", "selling price is empty or not a valid number", null);

        Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());

        WarehouseStocks stockRetrieved = warehouseStockRepository
                .findDistinctByStockNameAndWarehouse(stockName, seller.getWarehouse());

        if(stockRetrieved == null) throw new InventoryAPIResourceNotFoundException("Not found",
                "Stock with name: " + stockName + " was not found in your warehouse", null);

        return warehouseStockRepository.save(changeStockSellingPrice(stockRetrieved, newSellingPrice));
    }

    private WarehouseStocks addStockToWarehouse(WarehouseStocks stockToAdd, Warehouse warehouse){

        if (null == stockToAdd) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find stock entity to save", null);

        Supplier stockSupplier = stockToAdd.getLastRestockPurchasedFrom();

        StockCategory stockCategory = stockToAdd.getStockCategory();

        stockCategory = stockCategoryRepository.findDistinctFirstByCategoryName(stockCategory.getCategoryName());

        if (null == stockCategory) stockCategory = genericService.addStockCategory(stockToAdd.getStockCategory());

        stockSupplier = supplierRepository
                .findBySupplierPhoneNumber(stockSupplier.getSupplierPhoneNumber());

        if (null == stockSupplier) stockSupplier = genericService.addSupplier(stockToAdd.getLastRestockPurchasedFrom());

        WarehouseStocks existingStock = warehouseStockRepository
                .findDistinctByStockNameAndWarehouse(stockToAdd.getStockName(), warehouse);

        if (existingStock != null){

            existingStock.setLastRestockPurchasedFrom(stockSupplier);

            return reStockToWarehouse(warehouse.getWarehouseId(), existingStock.getWarehouseStockId(), stockToAdd);
        }

        Set<Supplier> supplierSet = new HashSet<>();
        supplierSet.add(stockSupplier);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            stockToAdd.setApproved(true);
            stockToAdd.setApprovedDate(new Date());
            stockToAdd.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
        }

        stockToAdd.setStockCategory(stockCategory);
        stockToAdd.setStockPurchasedFrom(supplierSet);
        stockToAdd.setWarehouse(warehouse);
        stockToAdd.setLastRestockPurchasedFrom(stockSupplier);
        stockToAdd.setLastRestockQuantity(stockToAdd.getStockQuantityPurchased());
        stockToAdd.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
        stockToAdd.setLastRestockBy(AuthenticatedUserDetails.getUserFullName());
        stockToAdd.setStockQuantityRemaining(stockToAdd.getStockQuantityPurchased());
        stockToAdd.setPossibleQuantityRemaining(stockToAdd.getStockQuantityRemaining());
        stockToAdd.setStockRemainingTotalPrice(stockToAdd.getStockPurchasedTotalPrice());
        stockToAdd.setPricePerStockPurchased(stockToAdd.getStockPurchasedTotalPrice()
                .divide(BigDecimal.valueOf(stockToAdd.getStockQuantityPurchased()), 2));

        return warehouseStockRepository.save(stockToAdd);
    }

    private WarehouseStocks changeStockSellingPrice(WarehouseStocks stock, BigDecimal newSellingPrice) {

        stock.setUpdateDate(new Date());
        stock.setSellingPricePerStock(newSellingPrice);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){
            stock.setApproved(true);
            stock.setApprovedDate(new Date());
            stock.setApprovedBy(AuthenticatedUserDetails.getUserFullName());

        }else{
            stock.setApproved(false);
            stock.setApprovedDate(null);
            stock.setApprovedBy(null);
        }

        return stock;
    }
}
