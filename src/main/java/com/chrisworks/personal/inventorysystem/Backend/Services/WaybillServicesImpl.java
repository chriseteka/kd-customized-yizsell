package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.*;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.chrisworks.personal.inventorysystem.Backend.Utility.UniqueIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;

/**
 * @author Chris_Eteka
 * @since 12/26/2019
 * @email chriseteka@gmail.com
 */
@Service
public class WaybillServicesImpl implements WaybillServices {

    private final SellerRepository sellerRepository;

    private final WaybillInvoiceRepository waybillInvoiceRepository;

    private final WarehouseRepository warehouseRepository;

    private final WarehouseStockRepository warehouseStockRepository;

    private final WarehouseStockServices warehouseStockServices;

    private final ShopStockServices shopStockServices;

    private final ExpenseServices expenseServices;

    private final ShopRepository shopRepository;

    private final GenericService genericService;

    @Autowired
    public WaybillServicesImpl(SellerRepository sellerRepository, WaybillInvoiceRepository waybillInvoiceRepository,
                               WarehouseRepository warehouseRepository, WarehouseStockRepository warehouseStockRepository,
                               WarehouseStockServices warehouseStockServices, ShopStockServices shopStockServices,
                               ExpenseServices expenseServices, ShopRepository shopRepository,
                               GenericService genericService) {
        this.sellerRepository = sellerRepository;
        this.waybillInvoiceRepository = waybillInvoiceRepository;
        this.warehouseRepository = warehouseRepository;
        this.warehouseStockServices = warehouseStockServices;
        this.warehouseStockRepository = warehouseStockRepository;
        this.shopStockServices = shopStockServices;
        this.expenseServices = expenseServices;
        this.shopRepository = shopRepository;
        this.genericService = genericService;
    }

    @Transactional
    @Override
    public WaybillInvoice requestStockFromWarehouse(Long warehouseId, List<WaybillOrder> stocks) {

        if (AuthenticatedUserDetails.getAccount_type() == null ||
                AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Unknown/Unauthorised user",
                "Operation not allowed, logged in user is not allowed to perform this operation", null);

        return warehouseRepository.findById(warehouseId).map(warehouse ->
                sellerRepository.findById(AuthenticatedUserDetails.getUserId()).map(seller -> {

                    if (null == seller.getShop()) throw new InventoryAPIOperationException("Seller has no shop",
                            "Seller has no shop and cannot request for stocks from any warehouse", null);

                    if (!seller.getCreatedBy().equalsIgnoreCase(warehouse.getCreatedBy()))
                        throw new InventoryAPIOperationException("Warehouse not yours",
                                "You cannot request for stock in a warehouse not created by your owner", null);

                    List<WarehouseStocks> warehouseStocks = warehouseStockRepository.findAllByWarehouse(warehouse);

                    if (warehouseStocks.isEmpty()) throw new InventoryAPIResourceNotFoundException
                            ("Could not find stocks", "No stock was found in the specified warehouse", null);

                    AtomicReference<Set<WaybilledStocks>> waybilledStocks = new AtomicReference<>(Collections.emptySet());
                    AtomicReference<BigDecimal> waybillInvoiceAmount = new AtomicReference<>(BigDecimal.ZERO);

                    waybilledStocks.set(
                        stocks.stream().map(order -> {
                            WarehouseStocks stockFound = warehouseStocks.stream()
                                    .filter(stock -> stock.getStockName().equalsIgnoreCase(order.getStockName()))
                                    .collect(toSingleton());

                            if (null == stockFound) throw new InventoryAPIResourceNotFoundException("Could not find stock",
                                    order.getStockName() + " in your list of orders was not found in the specified warehouse", null);

                            if (stockFound.getPossibleQuantityRemaining() >= order.getQuantity()){

                                stockFound.setPossibleQuantityRemaining(stockFound.getPossibleQuantityRemaining()
                                        - order.getQuantity());
                                WarehouseStocks updatedStock = warehouseStockRepository.save(stockFound);
                                warehouseStockServices.updateCache(updatedStock);
                                waybillInvoiceAmount.set(waybillInvoiceAmount.get()
                                        .add((BigDecimal.valueOf(order.getQuantity())
                                                .multiply(stockFound.getPricePerStockPurchased()))));
                                return new WaybilledStocks(order.getStockName(),
                                        stockFound.getStockCategory().getCategoryName(), order.getQuantity(),
                                        stockFound.getSellingPricePerStock(), stockFound.getPricePerStockPurchased(),
                                        stockFound.getExpiryDate(), stockFound.getStockBarCodeId(),
                                        stockFound.getLastRestockPurchasedFrom());
                            }
                            else  throw new InventoryAPIResourceNotFoundException("Limited stock in warehouse",
                                    order.getStockName() + " in your list of orders was found, but the number ordered is" +
                                            " above the number available in the warehouse.", null);
                        }).collect(Collectors.toSet())
                    );

                    if (waybilledStocks.get().isEmpty()) throw new InventoryAPIResourceNotFoundException
                            ("Orders not found", "Your orders were not found in the warehouse specified", null);

                    WaybillInvoice waybillInvoice = new WaybillInvoice();

                    waybillInvoice.setShop(seller.getShop());
                    waybillInvoice.setSellerRequesting(seller);
                    waybillInvoice.setWarehouse(warehouse);
                    waybillInvoice.setWaybilledStocks(waybilledStocks.get());
                    waybillInvoice.setWaybillInvoiceTotalAmount(waybillInvoiceAmount.get());
                    waybillInvoice.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
                    waybillInvoice.setWaybillInvoiceNumber(UniqueIdentifier.waybillInvoiceUID());

                    return waybillInvoiceRepository.save(waybillInvoice);
        }).orElse(null)).orElse(null);
    }

    @Transactional
    @Override
    public WaybillInvoice confirmAndShipWaybill(Long warehouseId, String waybillInvoiceNumber, BigDecimal expense) {

        if (AuthenticatedUserDetails.getAccount_type() == null ||
                AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER))
            throw new InventoryAPIOperationException("Unknown/Unauthorised user",
                    "Operation not allowed, logged in user is not allowed to perform this operation", null);

        WaybillInvoice waybillInvoice = waybillInvoiceRepository.findDistinctByWaybillInvoiceNumber(waybillInvoiceNumber);

        if (null == waybillInvoice) throw new InventoryAPIResourceNotFoundException("Waybill invoice not found",
                "waybill invoice with number: " + waybillInvoiceNumber + " was not found", null);

        Set<WaybilledStocks> waybilledStocks = waybillInvoice.getWaybilledStocks();

        if (waybilledStocks.isEmpty())
            throw new InventoryAPIOperationException("Invalid waybill invoice",
                    "Waybill invoice is invalid, this invoice contains no stock to be waybilled, review your inputs.", null);

        return warehouseRepository.findById(warehouseId).map(warehouse ->
                sellerRepository.findById(AuthenticatedUserDetails.getUserId()).map(seller -> {

                    if (null == seller.getWarehouse()) throw new InventoryAPIOperationException("Seller has no warehouse",
                            "Seller has no warehouse and cannot confirm and ship ware bills from any warehouse", null);

            if (!seller.getCreatedBy().equalsIgnoreCase(warehouse.getCreatedBy()))
                throw new InventoryAPIOperationException("Warehouse not yours",
                        "You cannot issue a waybill for stock in a warehouse not created by your owner", null);

            waybilledStocks.forEach(stock -> {

                WarehouseStocks stockFound = warehouseStockRepository
                        .findDistinctByStockNameAndWarehouse(stock.getStockName(), warehouse);

                //Decrementing of overall stock in the warehouse starts now, while profit, and stock sold total price increases
                stockFound.setStockQuantityRemaining(stockFound.getPossibleQuantityRemaining());
                stockFound.setStockQuantitySold(stockFound.getStockQuantitySold() + stock.getQuantityWaybilled());
                stockFound.setStockSoldTotalPrice(stockFound.getStockSoldTotalPrice().add(stock.getSellingPricePerStock()
                        .multiply(BigDecimal.valueOf(stock.getQuantityWaybilled()))));
                stockFound.setStockRemainingTotalPrice(BigDecimal.valueOf(stockFound.getStockQuantityRemaining())
                        .multiply(stockFound.getPricePerStockPurchased()));
                stockFound.setProfit(stockFound.getProfit().add(
                                (stockFound.getSellingPricePerStock().subtract(stockFound.getPricePerStockPurchased()))
                                        .multiply(BigDecimal.valueOf(stock.getQuantityWaybilled()))
                        ));
                WarehouseStocks updatedStock = warehouseStockRepository.save(stockFound);
                warehouseStockServices.updateCache(updatedStock);
            });

            //Create an expense for transporting stock from warehouse to shop requesting for waybill
            String expenseDesc = "Expense on transporting waybill with id: " + waybillInvoiceNumber;
            Expense expenseOnWaybill = new Expense(400, expense, expenseDesc);
            expenseOnWaybill.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
            expenseOnWaybill.setShop(waybillInvoice.getShop());
            expenseServices.createEntity(expenseOnWaybill);

            waybillInvoice.setSellerIssuing(seller);
            waybillInvoice.setIsWaybillShipped(true);
            waybillInvoice.setTimeShipped(new Date());
            waybillInvoice.setDateShipped(new Date());
            waybillInvoice.setWarehouse(seller.getWarehouse());
            waybillInvoice.setIssuedBy(AuthenticatedUserDetails.getUserFullName());
            return waybillInvoiceRepository.save(waybillInvoice);
        }).orElse(null)).orElse(null);
    }

    @Transactional
    @Override
    public WaybillInvoice confirmShipment(String waybillInvoiceNumber) {

        if (AuthenticatedUserDetails.getAccount_type() == null ||
                AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Unknown/Unauthorised user",
                    "Operation not allowed, logged in user is not allowed to perform this operation", null);

        WaybillInvoice waybillInvoice = waybillInvoiceRepository.findDistinctByWaybillInvoiceNumber(waybillInvoiceNumber);

        if (null == waybillInvoice) throw new InventoryAPIResourceNotFoundException("Waybill invoice not found",
                "waybill invoice with number: " + waybillInvoiceNumber + " was not found", null);

        if (!waybillInvoice.getIsWaybillShipped()) throw new InventoryAPIOperationException("Cannot confirm shipment",
                "Cannot confirm shipment of a stock that has not been moved from the requested warehouse", null);

        //Add each stock to the shop of the seller that requested the waybill
        waybillInvoice.getWaybilledStocks()
                .forEach(waybilledStocks -> {

                    Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());

                    if (seller == null || !seller.equals(waybillInvoice.getSellerRequesting()))
                        throw new InventoryAPIOperationException("Seller not acceptable", "Seller trying to confirm " +
                                "shipment may not have been the one who requested the waybill", null);

                    ShopStocks shopStock = new ShopStocks();

                    shopStock.setApproved(true);
                    shopStock.setApprovedDate(new Date());
                    shopStock.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
                    shopStock.setStockName(waybilledStocks.getStockName());
                    shopStock.setExpiryDate(waybilledStocks.getExpiryDate());
                    shopStock.setStockBarCodeId(waybilledStocks.getStockBarCodeId());
                    shopStock.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
                    shopStock.setLastRestockBy(AuthenticatedUserDetails.getUserFullName());
                    shopStock.setLastRestockQuantity(waybilledStocks.getQuantityWaybilled());
                    shopStock.setLastRestockPurchasedFrom(waybilledStocks.getStockSupplier());
                    shopStock.setStockQuantityRemaining(waybilledStocks.getQuantityWaybilled());
                    shopStock.setStockQuantityPurchased(waybilledStocks.getQuantityWaybilled());
                    shopStock.setSellingPricePerStock(waybilledStocks.getSellingPricePerStock());
                    shopStock.setPricePerStockPurchased(waybilledStocks.getPurchasePricePerStock());
                    shopStock.setStockRemainingTotalPrice(BigDecimal.valueOf(shopStock.getStockQuantityRemaining())
                            .multiply(waybilledStocks.getSellingPricePerStock()));
                    shopStock.setStockPurchasedTotalPrice(BigDecimal.valueOf(waybilledStocks.getQuantityWaybilled())
                            .multiply(waybilledStocks.getPurchasePricePerStock()));
                    shopStock.setStockCategory(genericService
                            .getAuthUserStockCategoryByCategoryName(waybilledStocks.getStockCategory()));

                    ShopStocks stockAddedToShop = shopStockServices.addStockToShop(shopStock, seller.getShop());

                    if (null == stockAddedToShop) throw new InventoryAPIOperationException("Operation failed",
                            "Could add stock with name: " + shopStock.getStockName() + " to shop, try again", null);
                });

        //Update the waybill invoice to reflect that the stock shipped has been received
        waybillInvoice.setIsWaybillReceived(true);
        waybillInvoice.setDateReceived(new Date());
        waybillInvoice.setTimeReceived(new Date());

        return waybillInvoiceRepository.save(waybillInvoice);
    }

    @Override
    public WaybillInvoice findByInvoiceNumber(String waybillInvoiceNumber) {

        if (AuthenticatedUserDetails.getAccount_type() == null ||
                AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER))
            throw new InventoryAPIOperationException("Unknown/Unauthorised user",
                    "Operation not allowed, logged in user is not allowed to perform this operation", null);

        WaybillInvoice waybillInvoice = waybillInvoiceRepository.findDistinctByWaybillInvoiceNumber(waybillInvoiceNumber);

        if (null == waybillInvoice) throw new InventoryAPIResourceNotFoundException
                ("Not found", "Waybill invoice with number: " + waybillInvoiceNumber + " was not found", null);

        if (waybillInvoice.getSellerIssuing().getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())
                && AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            return waybillInvoice;

        if (AuthenticatedUserDetails.getUserFullName().equalsIgnoreCase(waybillInvoice.getSellerRequesting().getCreatedBy()))
            return waybillInvoice;

        else throw new InventoryAPIOperationException("Invoice not yours", "This waybill invoice is not related to you", null);
    }

    @Override
    public WaybillInvoice findById(Long waybillInvoiceId) {

        if (AuthenticatedUserDetails.getAccount_type() == null ||
                AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER))
            throw new InventoryAPIOperationException("Unknown/Unauthorised user",
                    "Operation not allowed, logged in user is not allowed to perform this operation", null);

        return waybillInvoiceRepository.findById(waybillInvoiceId).map(waybillInvoice -> {

            if (waybillInvoice.getSellerIssuing().getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())
                    && AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
                return waybillInvoice;

            if (AuthenticatedUserDetails.getUserFullName().equalsIgnoreCase(waybillInvoice.getSellerRequesting().getCreatedBy()))
                return waybillInvoice;

            else throw new InventoryAPIOperationException("Invoice not yours", "This waybill invoice is not related to you", null);
        }).orElse(null);

    }

    @Override
    public List<WaybillInvoice> findAllInShop(Long shopId) {

        return shopRepository.findById(shopId)
                .map(shop -> {

                    if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)
                            && !sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails
                            .getUserFullName()).getCreatedBy().equalsIgnoreCase(shop.getCreatedBy()))
                        throw new InventoryAPIOperationException
                                ("Not your shop",
                                        "Cannot retrieve waybill invoice in a shop that is not created by your creator", null);

                    if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)
                            && !shop.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                        throw new InventoryAPIOperationException("Not your shop",
                                "Cannot retrieve waybill invoice in a shop that is not created by you", null);

                    return waybillInvoiceRepository.findAllByShop(shop);
                }).orElse(null);
    }

    @Override
    public List<WaybillInvoice> findAllInWarehouse(Long warehouseId) {

        if (AuthenticatedUserDetails.getAccount_type() == null ||
                AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER))
            throw new InventoryAPIOperationException("Unknown/Unauthorised user",
                    "Operation not allowed, logged in user is not allowed to perform this operation", null);

        return warehouseRepository.findById(warehouseId)
                .map(warehouse -> {

                    if (!warehouse.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                        throw new InventoryAPIOperationException("Not your warehouse",
                                "Cannot retrieve waybill invoice in a warehouse that is not created by you", null);

                    return waybillInvoiceRepository.findAllByWarehouse(warehouse);
                }).orElse(null);
    }

    @Override
    public List<WaybillInvoice> findAllByCreator(String createdBy) {

        if (AuthenticatedUserDetails.getAccount_type() == null ||
                !AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Unknown/Unauthorised user",
                    "Operation not allowed, logged in user is not allowed to perform this operation", null);

        Seller seller = sellerRepository.findDistinctBySellerFullNameOrSellerEmail(createdBy, createdBy);

        if (!seller.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
            throw new InventoryAPIOperationException("Not your seller",
                "You cannot retrieve waybills pertaining to this seller, because you did not create this seller", null);

        return waybillInvoiceRepository.findAllByCreatedBy(createdBy);
    }

    @Override
    public List<WaybillInvoice> findAllByIssuer(String issuedBy) {

        if (AuthenticatedUserDetails.getAccount_type() == null ||
                !AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Unknown/Unauthorised user",
                    "Operation not allowed, logged in user is not allowed to perform this operation", null);

        Seller seller = sellerRepository.findDistinctBySellerFullNameOrSellerEmail(issuedBy, issuedBy);

        if (!seller.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
            throw new InventoryAPIOperationException("Not your seller",
                    "You cannot retrieve waybills pertaining to this seller, because you did not create this seller", null);

        return waybillInvoiceRepository.findAllByIssuedBy(issuedBy);
    }

    @Override
    public List<WaybillInvoice> findAllByDateRequested(Date dateRequested) {

        if (AuthenticatedUserDetails.getAccount_type() == null ||
                !AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Unknown/Unauthorised user",
                    "Operation not allowed, logged in user is not allowed to perform this operation", null);

        return shopRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName())
                .stream()
                .map(waybillInvoiceRepository::findAllByShop)
                .flatMap(List::parallelStream)
                .filter(waybillInvoice -> waybillInvoice.getCreatedDate().equals(dateRequested))
                .collect(Collectors.toList());
    }

    @Override
    public List<WaybillInvoice> findAllByDateShipped(Date dateShipped) {

        if (AuthenticatedUserDetails.getAccount_type() == null ||
                !AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Unknown/Unauthorised user",
                    "Operation not allowed, logged in user is not allowed to perform this operation", null);

        return warehouseRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName())
                .stream()
                .map(waybillInvoiceRepository::findAllByWarehouse)
                .flatMap(List::parallelStream)
                .filter(waybillInvoice -> waybillInvoice.getDateShipped().equals(dateShipped))
                .collect(Collectors.toList());
    }

    @Override
    public List<WaybillInvoice> findAllDeliveredSuccessfully() {

        if (AuthenticatedUserDetails.getAccount_type() == null)
            throw new InventoryAPIOperationException("Unknown/Unauthorised user",
                    "Operation not allowed, logged in user is not allowed to perform this operation", null);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER))
            return sellerRepository.findById(AuthenticatedUserDetails.getUserId())
                    .map(seller -> waybillInvoiceRepository.findAllByShop(seller.getShop())
                            .stream()
                            .filter(WaybillInvoice::getIsWaybillReceived)
                            .collect(Collectors.toList())).orElse(Collections.emptyList());

        else if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            return sellerRepository.findById(AuthenticatedUserDetails.getUserId())
                    .map(seller -> waybillInvoiceRepository.findAllByWarehouse(seller.getWarehouse())
                            .stream()
                            .filter(WaybillInvoice::getIsWaybillReceived)
                            .collect(Collectors.toList())).orElse(Collections.emptyList());

        else if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            List<Seller> sellerList = sellerRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());
            Set<WaybillInvoice> waybillInvoices = sellerList.stream()
                    .map(Seller::getShop)
                    .map(waybillInvoiceRepository::findAllByShop)
                    .flatMap(List::parallelStream)
                    .filter(WaybillInvoice::getIsWaybillReceived)
                    .collect(Collectors.toSet());
            waybillInvoices.addAll(sellerList.stream()
                    .map(Seller::getWarehouse)
                    .map(waybillInvoiceRepository::findAllByWarehouse)
                    .flatMap(List::parallelStream)
                    .filter(WaybillInvoice::getIsWaybillReceived)
                    .collect(Collectors.toSet()));

            return new ArrayList<>(waybillInvoices);
        }
        return null;
    }

    @Override
    public List<WaybillInvoice> findAllPendingRequests() {

        if (AuthenticatedUserDetails.getAccount_type() == null)
            throw new InventoryAPIOperationException("Unknown/Unauthorised user",
                    "Operation not allowed, logged in user is not allowed to perform this operation", null);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER))
            return sellerRepository.findById(AuthenticatedUserDetails.getUserId())
                    .map(seller -> waybillInvoiceRepository.findAllByShop(seller.getShop())
                            .stream()
                            .filter(waybillInvoice -> !waybillInvoice.getIsWaybillReceived()
                                    && !waybillInvoice.getIsWaybillShipped())
                            .collect(Collectors.toList())).orElse(Collections.emptyList());

        else if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            return sellerRepository.findById(AuthenticatedUserDetails.getUserId())
                    .map(seller -> waybillInvoiceRepository.findAllByWarehouse(seller.getWarehouse())
                            .stream()
                            .filter(waybillInvoice -> !waybillInvoice.getIsWaybillReceived()
                                    && !waybillInvoice.getIsWaybillShipped())
                            .collect(Collectors.toList())).orElse(Collections.emptyList());

        else if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            List<Seller> sellerList = sellerRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());
            Set<WaybillInvoice> waybillInvoices = sellerList.stream()
                    .map(Seller::getShop)
                    .map(waybillInvoiceRepository::findAllByShop)
                    .flatMap(List::parallelStream)
                    .filter(waybillInvoice -> !waybillInvoice.getIsWaybillReceived()
                            && !waybillInvoice.getIsWaybillShipped())
                    .collect(Collectors.toSet());
            waybillInvoices.addAll(sellerList.stream()
                    .map(Seller::getWarehouse)
                    .map(waybillInvoiceRepository::findAllByWarehouse)
                    .flatMap(List::parallelStream)
                    .filter(waybillInvoice -> !waybillInvoice.getIsWaybillReceived()
                            && !waybillInvoice.getIsWaybillShipped())
                    .collect(Collectors.toSet()));

            return new ArrayList<>(waybillInvoices);
        }
        return null;
    }

    @Override
    public List<WaybillInvoice> findAllCurrentlyShipped() {

        if (AuthenticatedUserDetails.getAccount_type() == null)
            throw new InventoryAPIOperationException("Unknown/Unauthorised user",
                    "Operation not allowed, logged in user is not allowed to perform this operation", null);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER))
            return sellerRepository.findById(AuthenticatedUserDetails.getUserId())
                    .map(seller -> waybillInvoiceRepository.findAllByShop(seller.getShop())
                            .stream()
                            .filter(waybillInvoice -> waybillInvoice.getIsWaybillShipped()
                                    && !waybillInvoice.getIsWaybillReceived())
                            .collect(Collectors.toList())).orElse(Collections.emptyList());

        else if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            return sellerRepository.findById(AuthenticatedUserDetails.getUserId())
                    .map(seller -> waybillInvoiceRepository.findAllByWarehouse(seller.getWarehouse())
                            .stream()
                            .filter(waybillInvoice -> waybillInvoice.getIsWaybillShipped()
                                    && !waybillInvoice.getIsWaybillReceived())
                            .collect(Collectors.toList())).orElse(Collections.emptyList());

        else if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            List<Seller> sellerList = sellerRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());
            Set<WaybillInvoice> waybillInvoices = sellerList.stream()
                    .map(Seller::getShop)
                    .map(waybillInvoiceRepository::findAllByShop)
                    .flatMap(List::parallelStream)
                    .filter(waybillInvoice -> waybillInvoice.getIsWaybillShipped()
                            && !waybillInvoice.getIsWaybillReceived())
                    .collect(Collectors.toSet());
            waybillInvoices.addAll(sellerList.stream()
                    .map(Seller::getWarehouse)
                    .map(waybillInvoiceRepository::findAllByWarehouse)
                    .flatMap(List::parallelStream)
                    .filter(waybillInvoice -> waybillInvoice.getIsWaybillShipped()
                            && !waybillInvoice.getIsWaybillReceived())
                    .collect(Collectors.toSet()));

            return new ArrayList<>(waybillInvoices);
        }
        return null;
    }

    @Override
    public List<WaybilledStocks> allStocksInWaybillInvoiceId(Long waybillInvoiceId) {

        if (AuthenticatedUserDetails.getAccount_type() == null)
            throw new InventoryAPIOperationException("Unknown/Unauthorised user",
                    "Operation not allowed, logged in user is not allowed to perform this operation", null);

        return waybillInvoiceRepository.findById(waybillInvoiceId).map(waybillInvoice -> {

            Set<WaybilledStocks> waybilledStocks = waybillInvoice.getWaybilledStocks();

            if (waybillInvoice.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())
                    && (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)
                    || AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT)))
                return new ArrayList<>(waybilledStocks);

            else if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

                boolean matchFound = sellerRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName())
                        .stream()
                        .map(Seller::getSellerEmail)
                        .anyMatch(sellerEmail -> waybillInvoice.getCreatedBy()
                                .equalsIgnoreCase(sellerEmail));

                if (matchFound) return new ArrayList<>(waybilledStocks);
                else throw new InventoryAPIOperationException
                        ("Not your waybill invoice",
                                "The waybill invoice was not created by you or any of your sellers.", null);
            }
            else throw new InventoryAPIOperationException
                        ("Not your waybill invoice",
                                "The waybill invoice was not created by you.", null);
        }).orElse(new ArrayList<>());
    }

    @Override
    public List<WaybilledStocks> allStocksInWaybillInvoiceNumber(String waybillInvoiceNumber) {

        if (AuthenticatedUserDetails.getAccount_type() == null)
            throw new InventoryAPIOperationException("Unknown/Unauthorised user",
                    "Operation not allowed, logged in user is not allowed to perform this operation", null);

        WaybillInvoice waybillInvoice = waybillInvoiceRepository.findDistinctByWaybillInvoiceNumber(waybillInvoiceNumber);

        if (null == waybillInvoice) throw new InventoryAPIResourceNotFoundException
                ("Waybill invoice not found",
                        "Waybill invoice with number: " + waybillInvoiceNumber + " was not found", null);

        if (waybillInvoice.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())
                && (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)
                || AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT)))
            return new ArrayList<>(waybillInvoice.getWaybilledStocks());

        else if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            boolean matchFound = sellerRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName())
                    .stream()
                    .map(Seller::getSellerEmail)
                    .anyMatch(sellerEmail -> waybillInvoice.getCreatedBy()
                            .equalsIgnoreCase(sellerEmail));

            if (matchFound) return new ArrayList<>(waybillInvoice.getWaybilledStocks());
            else throw new InventoryAPIOperationException
                    ("Not your waybill invoice",
                            "The waybill invoice was not created by you or any of your sellers.", null);
        }

        else throw new InventoryAPIOperationException
                    ("Not your waybill invoice",
                            "The waybill invoice was not created by you.", null);
    }

    @Override
    public WaybillInvoice deleteWaybillInvoiceById(Long waybillInvoiceId) {

        if (null == AuthenticatedUserDetails.getAccount_type()
                || !AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Operation cannot be performed by the logged in user", null);

        return waybillInvoiceRepository.findById(waybillInvoiceId)
                .map(waybillInvoice -> {

                    List<Seller> sellers = sellerRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());

                    boolean anyMatch = sellers
                            .stream()
                            .map(Seller::getSellerEmail)
                            .anyMatch(sellerName -> sellerName.equalsIgnoreCase(waybillInvoice.getCreatedBy()));

                    if (anyMatch) waybillInvoiceRepository.delete(waybillInvoice);
                    else throw new InventoryAPIOperationException("Not allowed", "Operation not allowed, waybill was not created by " +
                            " any of your sellers", null);

                    return waybillInvoice;
                }).orElse(null);
    }

    @Override
    public List<WaybillInvoice> deleteWaybillInvoice(Long... waybillInvoiceId) {

        List<Long> waybillInvoiceIdsToDelete = Arrays.asList(waybillInvoiceId);

        if (waybillInvoiceIdsToDelete.size() == 1)
            return Collections.singletonList(deleteWaybillInvoiceById(waybillInvoiceIdsToDelete.get(0)));

        List<WaybillInvoice> waybillInvoicesToDelete = waybillInvoiceRepository.findAll().stream()
                .filter(waybillInvoice -> waybillInvoiceIdsToDelete.contains(waybillInvoice.getWaybillInvoiceId()))
                .collect(Collectors.toList());

        if (!waybillInvoicesToDelete.isEmpty()) waybillInvoiceRepository.deleteAll(waybillInvoicesToDelete);

        return waybillInvoicesToDelete;
    }
}
