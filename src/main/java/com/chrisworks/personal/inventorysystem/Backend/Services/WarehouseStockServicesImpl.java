package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.BulkUploadResponseWrapper;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.WarehouseStocks;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Supplier;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockCategory;
import com.chrisworks.personal.inventorysystem.Backend.Entities.UniqueStock;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDuplicateEntryException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.*;
import com.chrisworks.personal.inventorysystem.Backend.Services.CacheManager.Interfaces.CacheInterface;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Entities.BulkUploadResponseWrapper.bulkUploadResponse;
import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.*;
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

    private final CacheInterface<com.chrisworks.personal.inventorysystem.Backend.Entities.DTO.WarehouseStocks> warehouseStocksCacheManager;
    private final String REDIS_TABLE_KEY = "WAREHOUSE_STOCK";

    @Autowired
    public WarehouseStockServicesImpl(SellerRepository sellerRepository, WarehouseRepository warehouseRepository,
                                      WarehouseStockRepository warehouseStockRepository, GenericService genericService,
                                      StockCategoryRepository stockCategoryRepository, SupplierRepository supplierRepository,
                                      CacheInterface<com.chrisworks.personal.inventorysystem.Backend.Entities.DTO.WarehouseStocks> warehouseStocksCacheManager) {
        this.sellerRepository = sellerRepository;
        this.warehouseRepository = warehouseRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.genericService = genericService;
        this.stockCategoryRepository = stockCategoryRepository;
        this.supplierRepository = supplierRepository;
        this.warehouseStocksCacheManager = warehouseStocksCacheManager;
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
                    }).orElseThrow(() -> new InventoryAPIResourceNotFoundException("Seller not found",
                                "Could not determine the warehouse attendant making this request", null));
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
        }).orElseThrow(()-> new InventoryAPIOperationException("Warehouse not found",
                "Warehouse with id: " + warehouseId + " was not found.", null));
    }

    @Override
    @Transactional
    public BulkUploadResponseWrapper createStockListInWarehouse(Long warehouseId, List<WarehouseStocks> stocksList) {

        if (AuthenticatedUserDetails.getAccount_type() == null) throw new InventoryAPIOperationException
                ("Unknown user", "Could not identify the type of user trying to add stock to warehouse", null);

        return warehouseRepository.findById(warehouseId).map(warehouse -> {

            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT)){

                return sellerRepository.findById(AuthenticatedUserDetails.getUserId())
                        .map(seller -> {

                            if (!seller.getCreatedBy().equalsIgnoreCase(warehouse.getCreatedBy())) throw new
                                    InventoryAPIOperationException
                                    ("Not your warehouse", "Warehouse does not belong to your creator", null);

                            return bulkUploadToWarehouseStock(stocksList, warehouse);
                        }).orElse(null);
            }
            else if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)
                    && AuthenticatedUserDetails.getHasWarehouse()){

                if (!warehouse.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())) throw new
                        InventoryAPIOperationException("Not your warehouse", "Warehouse you are about to add a stock" +
                        " does not belong to you, cannot proceed with this operation", null);

                return bulkUploadToWarehouseStock(stocksList, warehouse);
            }
            else throw new InventoryAPIOperationException
                        ("Operation not allowed", "User attempting this operation is not allowed to proceed", null);
        }).orElseThrow(()-> new InventoryAPIOperationException("Warehouse not found",
                "Warehouse with id: " + warehouseId + " was not found.", null));
    }

    @Override
    public List<WarehouseStocks> allStockByWarehouseId(Long warehouseId) {

        if (AuthenticatedUserDetails.getAccount_type() == null) throw new InventoryAPIOperationException
                ("Unknown user", "Could not identify the type of user trying to view all stock in warehouse", null);

        return warehouseRepository.findById(warehouseId)
                .map(warehouse -> {

                    if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)
                            && !sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails
                            .getUserFullName()).getCreatedBy().equalsIgnoreCase(warehouse.getCreatedBy()))
                        throw new InventoryAPIOperationException
                                ("Not your warehouse", "Warehouse does not belong to your creator", null);

                    if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)
                            && !warehouse.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                        throw new InventoryAPIOperationException("Not your warehouse", "You cannot retrieve stock from" +
                                " this warehouse because it was not created by you", null);

                    List<WarehouseStocks> warehouseStocksListByWarehouse;
                    if (warehouseStocksCacheManager.nonEmpty(REDIS_TABLE_KEY)) {
                        warehouseStocksListByWarehouse = fetchWarehouseStocksFromCache().stream()
                                .filter(s -> s.getWarehouse().getWarehouseId().equals(warehouseId)).collect(Collectors.toList());

                        if (warehouseStocksListByWarehouse.isEmpty()){

                            warehouseStocksListByWarehouse = warehouseStockRepository.findAllByWarehouse(warehouse);
                            cacheWarehouseStocksList(warehouseStocksListByWarehouse);
                        }
                    }

                    warehouseStocksListByWarehouse = warehouseStockRepository.findAllByWarehouse(warehouse);
                    cacheWarehouseStocksList(warehouseStocksListByWarehouse);

                    return warehouseStocksListByWarehouse;
                }).orElseThrow(() -> new InventoryAPIResourceNotFoundException("Warehouse not found",
                        "Warehouse with id: " + warehouseId + " was not found", null));

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
                .filter(warehouseStocks -> warehouseStocks.getExpiryDate() != null)
                .filter(warehouseStocks -> getDateDifferenceInDays(expiryDateInterval, warehouseStocks.getExpiryDate()) <= 60)
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

                    WarehouseStocks updatedStock = warehouseStockRepository.save(stockFound);
                    updateWarehouseStockCache(updatedStock);

                    return updatedStock;
                }).orElseThrow(() -> new InventoryAPIResourceNotFoundException("Stock not found",
                        "Stock with id: " + stockId + " was not found", null));
    }

    @Override
    public List<WarehouseStocks> approveStockList(List<Long> stockIdList) {

        return stockIdList.stream()
                .map(this::approveStock)
                .collect(Collectors.toList());
    }

    @Override
    public List<WarehouseStocks> deleteStock(Long warehouseId, Long... stockId) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) throw new
                InventoryAPIOperationException("Not allowed", "Operation not allowed for logged in user.", null);

        List<Long> stockIdsToBeDeleted = Arrays.asList(stockId);
        List<WarehouseStocks> warehouseStocks = allStockByWarehouseId(warehouseId).stream()
                .filter(s -> stockIdsToBeDeleted.contains(s.getWarehouseStockId()))
                .collect(Collectors.toList());

        if (!warehouseStocks.isEmpty()) {
            warehouseStockRepository.deleteAll(warehouseStocks);
            warehouseStocks.forEach(s -> warehouseStocksCacheManager.removeDetail(REDIS_TABLE_KEY, s.getWarehouseStockId()));
        }

        return warehouseStocks;
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

            Supplier finalStockSupplier = genericService.addSupplier(newStock.getLastRestockPurchasedFrom());

            Optional<WarehouseStocks> optionalStock = warehouseStockRepository.findById(stockId);

            if (!optionalStock.isPresent()) throw new InventoryAPIOperationException
                    ("could not find an entity", "Could not find stock with the id " + stockId, null);

            return optionalStock.map(stock -> {

                if (stock.getExpiryDate() != null
                        && !stock.getStockName().equalsIgnoreCase(newStock.getStockName()))
                    throw new InventoryAPIOperationException("Stock name mismatch", "Cannot proceed with the restock," +
                            " because incoming stock comes with an expiration date not matching existing stock", null);

                List<BigDecimal> oldAndNewSellingPrices = Arrays.asList(stock.getSellingPricePerStock(), newStock.getSellingPricePerStock());
                List<BigDecimal> oldAndNewPurchasePrices = Arrays.asList(stock.getPricePerStockPurchased(), newStock.getPricePerStockPurchased());
                List<Integer> oldAndNewStockQuantity = Arrays.asList(stock.getStockQuantityRemaining(), newStock.getStockQuantityPurchased());
                Set<Supplier> allSuppliers = stock.getStockPurchasedFrom();
                allSuppliers.add(finalStockSupplier);
                stock.setUpdateDate(new Date());
                stock.setStockPurchasedFrom(allSuppliers);
                stock.setLastRestockPurchasedFrom(finalStockSupplier);
                stock.setLastRestockBy(AuthenticatedUserDetails.getUserFullName());
                stock.setLastRestockQuantity(newStock.getStockQuantityPurchased());
                stock.setPricePerStockPurchased(computeWeightedPrice(oldAndNewStockQuantity, oldAndNewPurchasePrices));
                stock.setSellingPricePerStock(computeWeightedPrice(oldAndNewStockQuantity, oldAndNewSellingPrices));
                stock.setStockQuantityPurchased(newStock.getStockQuantityPurchased() + stock.getStockQuantityPurchased());
                stock.setStockQuantityRemaining(newStock.getStockQuantityPurchased() + stock.getStockQuantityRemaining());
                stock.setPossibleQuantityRemaining(newStock.getStockQuantityPurchased() + stock.getPossibleQuantityRemaining());
                newStock.setStockPurchasedTotalPrice(newStock.getPricePerStockPurchased()
                        .multiply(BigDecimal.valueOf(newStock.getStockQuantityPurchased())));
                stock.setStockPurchasedTotalPrice((newStock.getStockPurchasedTotalPrice()).add(stock.getStockPurchasedTotalPrice()));
                stock.setStockRemainingTotalPrice(newStock.getStockPurchasedTotalPrice().add(stock.getStockRemainingTotalPrice()));

                if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

                    stock.setApproved(true);
                    stock.setApprovedDate(new Date());
                    stock.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
                }

                WarehouseStocks updatedStock = warehouseStockRepository.save(stock);
                if (warehouseStocksCacheManager.nonEmpty(REDIS_TABLE_KEY)) updateWarehouseStockCache(updatedStock);

                return updatedStock;
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

        WarehouseStocks updatedWarehouseStock = updatedStock.get();
        updateWarehouseStockCache(updatedWarehouseStock);

        return updatedWarehouseStock;
    }

    @Override
    public WarehouseStocks changeStockSellingPriceByWarehouseIdAndStockName(Long warehouseId, String stockName, BigDecimal newSellingPrice) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Not allowed",
                    "Operation not allowed, this action can only be performed by a warehouse attendant", null);

        if (null == warehouseId || warehouseId < 0 || !warehouseId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("warehouse id error", "warehouse id is empty or not a valid number", null);

        if (null == stockName || stockName.isEmpty()) throw new
                InventoryAPIOperationException("stock name error", "stock name is empty or null", null);

        if (null == newSellingPrice || is(newSellingPrice).lte(BigDecimal.ZERO) || !newSellingPrice.toString().matches("\\d+"))
            throw new InventoryAPIOperationException("selling price error", "selling price is empty or not a valid number", null);

        Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());

        Warehouse warehouse = seller.getWarehouse();

        if (null == warehouse) throw new InventoryAPIOperationException("Seller has not warehouse",
                "Seller performing this operation may have not been assigned to a warehouse", null);

        WarehouseStocks stockRetrieved = warehouseStockRepository
                .findDistinctByStockNameAndWarehouse(stockName, warehouse);

        if(stockRetrieved == null) throw new InventoryAPIResourceNotFoundException("Not found",
                "Stock with name: " + stockName + " was not found in your warehouse", null);

        WarehouseStocks updatedStock = warehouseStockRepository.save(changeStockSellingPrice(stockRetrieved, newSellingPrice));
        updateWarehouseStockCache(updatedStock);

        return updatedStock;
    }

    @Override
    public List<UniqueStock> fetchAllAuthUserUniqueStocks(Long warehouseId) {

        List<String> stockNames = new ArrayList<>(Collections.emptyList());

        List<WarehouseStocks> warehouseStocksList = this.allStockByWarehouseId(warehouseId);

        stockNames.addAll(
                warehouseStocksList
                        .parallelStream()
                        .map(warehouseStocks -> stripStringOfExpiryDate(warehouseStocks.getStockName()))
                        .distinct()
                        .collect(Collectors.toList())
        );

        return stockNames
                .stream()
                .map(s -> warehouseStocksList
                        .stream()
                        .filter(warehouseStocks -> stripStringOfExpiryDate(warehouseStocks.getStockName()).equalsIgnoreCase(s))
                        .reduce(new WarehouseStocks(), (s1, s2) -> {

                            if (s1 == null || StringUtils.isEmpty(s1.getStockName())) return s2;
                            String s1Name = stripStringOfExpiryDate(s1.getStockName());
                            if (s1Name.equalsIgnoreCase(stripStringOfExpiryDate(s2.getStockName()))) {
                                s1.setStockQuantityRemaining(s1.getStockQuantityPurchased()
                                        + s2.getStockQuantityRemaining());
                            }
                            return s1;
                        }))
                .map(ws -> new UniqueStock
                        (ws.getWarehouseStockId(), stripStringOfExpiryDate(ws.getStockName()),
                                ws.getStockQuantityRemaining(), ws.getPricePerStockPurchased(), ws.getSellingPricePerStock()))
                .collect(Collectors.toList());
    }

    @Override
    public WarehouseStocks forceChangeStockQuantity(Long stockId, int newQuantity) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) throw new
                InventoryAPIOperationException("Not allowed", "Operation not allowed for logged in user.", null);

        return warehouseStockRepository.findById(stockId).map(warehouseStock -> {

            if (!warehouseStock.getWarehouse().getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Warehouse Stock not yours",
                        "You are attempting to modify a warehouse stock that is not in your warehouse", null);

            warehouseStock.setLastRestockBy(AuthenticatedUserDetails.getUserFullName());
            warehouseStock.setStockQuantitySold(0);
            warehouseStock.setStockQuantityRemaining(newQuantity);
            warehouseStock.setStockQuantityPurchased(newQuantity);
            warehouseStock.setStockPurchasedTotalPrice(BigDecimal.valueOf(newQuantity)
                    .multiply(warehouseStock.getPricePerStockPurchased()));
            warehouseStock.setPossibleQuantityRemaining(newQuantity);
            warehouseStock.setStockRemainingTotalPrice(BigDecimal.valueOf(newQuantity)
                    .multiply(warehouseStock.getSellingPricePerStock()));
            warehouseStock.setLastRestockQuantity(newQuantity);
            warehouseStock.setProfit(BigDecimal.ZERO);
            warehouseStock.setStockSoldTotalPrice(BigDecimal.ZERO);
            warehouseStock.setUpdateDate(new Date());

            WarehouseStocks updatedStock = warehouseStockRepository.save(warehouseStock);
            updateWarehouseStockCache(updatedStock);

            return updatedStock;
        }).orElseThrow(() -> new InventoryAPIResourceNotFoundException("Warehouse stock not found",
                "warehouse stock with id: " + stockId + " was not found", null));
    }

    @Override
    public void updateCache(WarehouseStocks stocks) {
        updateWarehouseStockCache(stocks);
    }

    private WarehouseStocks addStockToWarehouse(WarehouseStocks stockToAdd, Warehouse warehouse){

        if (null == stockToAdd) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find stock entity to save", null);

        Supplier stockSupplier = genericService.addSupplier(stockToAdd.getLastRestockPurchasedFrom());

        StockCategory stockCategory = genericService.addStockCategory(stockToAdd.getStockCategory());

        if (!StringUtils.isEmpty(stockToAdd.getStockBarCodeId())) {

            WarehouseStocks stockByBarcode = warehouseStockRepository
                    .findDistinctByStockBarCodeId(stockToAdd.getStockBarCodeId());
            if (stockByBarcode != null
                && !stockByBarcode.getStockName().equalsIgnoreCase(stockToAdd.getStockName())){

                throw new InventoryAPIDuplicateEntryException("Barcode already exist",
                        "Another stock exist with the barcode id you passed for the new stock you are about to add", null);
            }
        }

        if (stockToAdd.getExpiryDate() != null)
            stockToAdd.setStockName(stockToAdd.getStockName() + " Exp: " + formatDate(stockToAdd.getExpiryDate()));

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
        stockToAdd.setStockPurchasedTotalPrice(stockToAdd.getPricePerStockPurchased()
                .multiply(BigDecimal.valueOf(stockToAdd.getStockQuantityPurchased())));
        stockToAdd.setStockRemainingTotalPrice(stockToAdd.getStockPurchasedTotalPrice());

        WarehouseStocks savedStock = warehouseStockRepository.save(stockToAdd);
        if (warehouseStocksCacheManager.nonEmpty(REDIS_TABLE_KEY)) cacheWarehouseStocks(savedStock);

        return savedStock;
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

    private BulkUploadResponseWrapper bulkUploadToWarehouseStock(List<WarehouseStocks> stocksList, Warehouse warehouse){

        //Remove any duplicate before you persist the data (duplicates search by name)
        List<WarehouseStocks> stockToAddList;
        List<String> stockNames = stocksList
                .stream()
                .map(WarehouseStocks::getStockName)
                .distinct()
                .collect(Collectors.toList());

        //Merge quantities for duplicate stock
        stockToAddList = stockNames
                .stream()
                .map(name -> stocksList
                        .stream()
                        .filter(stock -> stock.getStockName().equalsIgnoreCase(name))
                        .reduce(new WarehouseStocks(), (t1, t2) -> {

                            if (t1.getStockName() == null) return t2;
                            if ((t1.getExpiryDate() != null && t2.getExpiryDate() != null
                                    && t1.getExpiryDate().equals(t2.getExpiryDate()))
                                    || t1.getStockName().equalsIgnoreCase(t2.getStockName())) {


                                t1.setStockQuantityPurchased(t1.getStockQuantityPurchased() + t2.getStockQuantityPurchased());
                            }
                            return t1;
                        }))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<WarehouseStocks> rejectedStockList = new ArrayList<>(Collections.emptyList());
        List<StockCategory> existingCategories;
        List<Supplier> existingSuppliers;
        String creator = AuthenticatedUserDetails.getUserFullName();

        existingCategories = genericService.getAuthUserStockCategories();
        existingSuppliers = genericService.getAuthUserSuppliers();

        //Already existing stock category names
        List<String> categoryNames = existingCategories
            .stream()
            .map(StockCategory::getCategoryName)
            .collect(Collectors.toList());

        //Already existing suppliers phone numbers
        List<String> suppliersNumbers = existingSuppliers
            .stream()
            .map(Supplier::getSupplierPhoneNumber)
            .collect(Collectors.toList());

        List<String> incomingStockCategoryNames = stockToAddList
            .stream()
            .map(WarehouseStocks::getStockCategory)
            .map(StockCategory::getCategoryName)
            .distinct()
            .collect(Collectors.toList());

        //Create a new stock category if stock category does not exist.
        incomingStockCategoryNames
            .forEach(categoryName -> {
                if (!categoryNames.contains(categoryName)){

                    StockCategory temporaryCategory = new StockCategory();
                    temporaryCategory.setCategoryName(categoryName);
                    temporaryCategory.setCreatedBy(creator);
                    existingCategories.add(stockCategoryRepository.save(temporaryCategory));
                }
            });

        Map<String, List<Supplier>> incomingSuppliers = stockToAddList
            .stream()
            .map(WarehouseStocks::getLastRestockPurchasedFrom)
            .collect(Collectors.groupingBy(Supplier::getSupplierPhoneNumber, Collectors.toList()));

        //Save a new supplier if incoming supplier does not exist.
        for (String key : incomingSuppliers.keySet()) {

            Supplier supplier = incomingSuppliers.get(key).get(0);
                if (!suppliersNumbers.contains(supplier.getSupplierPhoneNumber())) {

                    supplier.setCreatedBy(creator);
                    existingSuppliers.add(supplierRepository.save(supplier));
                }
        }

        final List<StockCategory> registeredCategories = existingCategories;

        final List<Supplier> registeredSuppliers = existingSuppliers;

        List<WarehouseStocks> stockToSaveList = stockToAddList
            .stream()
            .map(stockToAdd -> {

                //If there is bar code, do this
                if (!StringUtils.isEmpty(stockToAdd.getStockBarCodeId())) {

                    WarehouseStocks stockByBarcode = warehouseStockRepository
                            .findDistinctByStockBarCodeId(stockToAdd.getStockBarCodeId());
                    if (stockByBarcode != null
                            && !stockByBarcode.getStockName().equalsIgnoreCase(stockToAdd.getStockName())) {

                        throw new InventoryAPIDuplicateEntryException("Barcode already exist",
                                "Another stock exist with the barcode id you passed for the new stock you are about to add", null);
                    }
                }

                //If stock has quantity from zero and below
                if (stockToAdd.getStockQuantityPurchased() < 0){

                    rejectedStockList.add(stockToAdd);
                    return null;
                }

                //If stock has expiry date, rename with inclusion of the date
                if (stockToAdd.getExpiryDate() != null)
                    stockToAdd.setStockName(stockToAdd.getStockName() + " Exp: " + formatDate(stockToAdd.getExpiryDate()));

                stockToAdd.setStockCategory(registeredCategories
                        .stream()
                        .filter(stockCategory -> stockCategory.getCategoryName()
                                .equalsIgnoreCase(stockToAdd.getStockCategory().getCategoryName()))
                        .collect(toSingleton())
                );
                stockToAdd.setLastRestockPurchasedFrom(registeredSuppliers
                        .stream()
                        .filter(supplier -> supplier.getSupplierPhoneNumber()
                                .equalsIgnoreCase(stockToAdd.getLastRestockPurchasedFrom()
                                        .getSupplierPhoneNumber()))
                        .collect(toSingleton())
                );

                WarehouseStocks existingStock = warehouseStockRepository
                        .findDistinctByStockNameAndWarehouse(stockToAdd.getStockName(), warehouse);

                //If stock already exist, increment its quantity, and make an update
                if (existingStock != null) {

                    if (existingStock.getExpiryDate() != null
                            && !stockToAdd.getStockName().equalsIgnoreCase(existingStock.getStockName()))
                        throw new InventoryAPIOperationException("Stock name mismatch", "Cannot proceed with the restock," +
                                " because incoming stock comes with an expiration date not matching existing stock", null);

                    existingStock.setLastRestockPurchasedFrom(stockToAdd.getLastRestockPurchasedFrom());
                    Set<Supplier> allSuppliers = existingStock.getStockPurchasedFrom();
                    allSuppliers.add(existingStock.getLastRestockPurchasedFrom());
                    existingStock.setUpdateDate(new Date());
                    existingStock.setStockPurchasedFrom(allSuppliers);
                    existingStock.setLastRestockBy(AuthenticatedUserDetails.getUserFullName());
                    existingStock.setLastRestockQuantity(stockToAdd.getStockQuantityPurchased());
                    existingStock.setSellingPricePerStock(stockToAdd.getSellingPricePerStock());
                    existingStock.setStockQuantityPurchased(stockToAdd.getStockQuantityPurchased() + existingStock.getStockQuantityPurchased());
                    existingStock.setStockQuantityRemaining(stockToAdd.getStockQuantityPurchased() + existingStock.getStockQuantityRemaining());
                    existingStock.setPossibleQuantityRemaining(stockToAdd.getStockQuantityPurchased() + existingStock.getPossibleQuantityRemaining());
                    stockToAdd.setStockPurchasedTotalPrice(stockToAdd.getPricePerStockPurchased()
                            .multiply(BigDecimal.valueOf(stockToAdd.getStockQuantityPurchased())));
                    existingStock.setStockPurchasedTotalPrice(stockToAdd.getStockPurchasedTotalPrice().add(existingStock.getStockPurchasedTotalPrice()));
                    existingStock.setStockRemainingTotalPrice(stockToAdd.getStockPurchasedTotalPrice().add(existingStock.getStockRemainingTotalPrice()));

                    if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

                        existingStock.setApproved(true);
                        existingStock.setApprovedDate(new Date());
                        existingStock.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
                    }
                    return existingStock;
                }
                else {
                    //If stock is uploaded by business owner, approve it
                    if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

                        stockToAdd.setApproved(true);
                        stockToAdd.setApprovedDate(new Date());
                        stockToAdd.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
                    }

                    //Set other fields
                    stockToAdd.setWarehouse(warehouse);
                    stockToAdd.setLastRestockQuantity(stockToAdd.getStockQuantityPurchased());
                    stockToAdd.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
                    stockToAdd.setLastRestockBy(AuthenticatedUserDetails.getUserFullName());
                    stockToAdd.setStockQuantityRemaining(stockToAdd.getStockQuantityPurchased());
                    stockToAdd.setPossibleQuantityRemaining(stockToAdd.getStockQuantityRemaining());
                    stockToAdd.setStockPurchasedTotalPrice(stockToAdd.getPricePerStockPurchased()
                            .multiply(BigDecimal.valueOf(stockToAdd.getStockQuantityPurchased())));
                    stockToAdd.setStockRemainingTotalPrice(stockToAdd.getStockPurchasedTotalPrice());
                    Set<Supplier> supplierSet = new HashSet<>();
                    supplierSet.add(stockToAdd.getLastRestockPurchasedFrom());
                    stockToAdd.setStockPurchasedFrom(supplierSet);

                    return stockToAdd;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        List<WarehouseStocks> successfulUploads = warehouseStockRepository.saveAll(stockToSaveList);
        if (warehouseStocksCacheManager.nonEmpty(REDIS_TABLE_KEY)) cacheWarehouseStocksList(successfulUploads);

        return bulkUploadResponse(successfulUploads, rejectedStockList);
    }

    private void updateWarehouseStockCache(WarehouseStocks warehouseStocks){
        warehouseStocksCacheManager.updateCacheDetail(REDIS_TABLE_KEY, warehouseStocks.toDTO(), warehouseStocks.getWarehouseStockId());
    }

    private void cacheWarehouseStocks(WarehouseStocks warehouseStocks){
        warehouseStocksCacheManager.cacheDetail(REDIS_TABLE_KEY, warehouseStocks.toDTO(), warehouseStocks.getWarehouseStockId());
    }

    private void cacheWarehouseStocksList(List<WarehouseStocks> shopStocksList) {
        shopStocksList.forEach(this::cacheWarehouseStocks);
    }

    private List<WarehouseStocks> fetchWarehouseStocksFromCache(){
        return warehouseStocksCacheManager.fetchDetailsByKey(REDIS_TABLE_KEY, data ->
                new ArrayList<>(getGSon().fromJson(data.stream()
                                .map(entry -> getGSon().toJson(entry.getValue()))
                                .collect(Collectors.toList()).toString(),
                        new TypeToken<ArrayList<com.chrisworks.personal.inventorysystem.Backend.Entities.DTO.WarehouseStocks>>(){}.getType())))
                .stream().map(com.chrisworks.personal.inventorysystem.Backend.Entities.DTO.WarehouseStocks::fromDTO)
                .collect(Collectors.toList());
    }
}
