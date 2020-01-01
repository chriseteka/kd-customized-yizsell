package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDuplicateEntryException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.*;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.chrisworks.personal.inventorysystem.Backend.Utility.UniqueIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.getDateDifferenceInDays;
import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;
import static ir.cafebabe.math.utils.BigDecimalUtils.is;

/**
 * @author Chris_Eteka
 * @since 12/26/2019
 * @email chriseteka@gmail.com
 */
@Service
public class ShopStockServicesImpl implements ShopStockServices {

    private final SellerRepository sellerRepository;

    private final ShopStocksRepository shopStocksRepository;

    private final ShopRepository shopRepository;

    private final GenericService genericService;

    private final StockCategoryRepository stockCategoryRepository;

    private final SupplierRepository supplierRepository;

    private final InvoiceRepository invoiceRepository;

    private final CustomerRepository customerRepository;

    private final StockSoldRepository stockSoldRepository;

    private final ReturnedStockRepository returnedStockRepository;

    @Autowired
    public ShopStockServicesImpl(SellerRepository sellerRepository, ShopStocksRepository shopStocksRepository,
                                 ShopRepository shopRepository, StockCategoryRepository stockCategoryRepository,
                                 GenericService genericService, SupplierRepository supplierRepository,
                                 InvoiceRepository invoiceRepository, CustomerRepository customerRepository,
                                 StockSoldRepository stockSoldRepository, ReturnedStockRepository returnedStockRepository) {
        this.sellerRepository = sellerRepository;
        this.shopStocksRepository = shopStocksRepository;
        this.shopRepository = shopRepository;
        this.genericService = genericService;
        this.stockCategoryRepository = stockCategoryRepository;
        this.supplierRepository = supplierRepository;
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.stockSoldRepository = stockSoldRepository;
        this.returnedStockRepository = returnedStockRepository;
    }

    @Transactional
    @Override
    public ShopStocks createStockInShop(Long shopId, ShopStocks stock) {

        if (AuthenticatedUserDetails.getAccount_type() == null) throw new InventoryAPIOperationException
                ("Unknown user", "Could not identify the type of user trying to add stock to shop", null);

        return shopRepository.findById(shopId).map(shop -> {

            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)){

                return sellerRepository.findById(AuthenticatedUserDetails.getUserId())
                        .map(seller -> {

                            if (!seller.getCreatedBy().equalsIgnoreCase(shop.getCreatedBy())) throw new
                                    InventoryAPIOperationException
                                    ("Not your Shop", "Shop does not belong to your creator", null);

                            System.out.println(shop.getBusinessOwner().getHasWarehouse());
                            if (shop.getBusinessOwner().getHasWarehouse())throw new InventoryAPIOperationException
                                    ("Operation not allowed", "You cannot add stock directly to this shop, you must" +
                                            " first request a waybill from any of the business owner warehouses", null);

                            return addStockToShop(stock, shop);
                        }).orElse(null);
            }
            else if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

                if (!shop.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())) throw new
                        InventoryAPIOperationException("Not your Shop", "Shop you are about to add a stock" +
                        " does not belong to you, cannot proceed with this operation", null);

                if (AuthenticatedUserDetails.getHasWarehouse()) throw new InventoryAPIOperationException
                        ("Operation not allowed", "You cannot add stock directly to this shop, you must" +
                                " first request a waybill from any of the business owner warehouses", null);

                return addStockToShop(stock, shop);
            }
            else throw new InventoryAPIOperationException
                        ("Operation not allowed", "User attempting this operation is not allowed to proceed", null);
        }).orElse(null);
    }

    @Override
    public List<ShopStocks> allStockByShopId(Long shopId) {

        if (AuthenticatedUserDetails.getAccount_type() == null) throw new InventoryAPIOperationException
                ("Unknown user", "Could not identify the type of user trying to view all stock in shop", null);

        return shopRepository.findById(shopId)
                .map(shop -> {

                    if ((AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT)
                            || AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER))
                            && !sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails
                            .getUserFullName()).getCreatedBy().equalsIgnoreCase(shop.getCreatedBy()))
                        throw new InventoryAPIOperationException
                                ("Not your shop", "Shop does not belong to your creator", null);

                    if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)
                            && !shop.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                        throw new InventoryAPIOperationException("Not your shop", "You cannot retrieve stock from" +
                                " this shop because it was not created by you", null);

                    return shopStocksRepository.findAllByShop(shop);
                }).orElse(Collections.emptyList());
    }

    @Override
    public List<ShopStocks> allSoonToFinishStock(Long shopId, int limit) {

        return this.allStockByShopId(shopId)
                .stream()
                .filter(shopStocks -> shopStocks.getStockQuantityRemaining() <= limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShopStocks> allSoonToExpireStock(Long shopId, Date expiryDateInterval) {

        return this.allStockByShopId(shopId)
                .stream()
                .filter(shopStocks -> shopStocks.getExpiryDate() != null)
                .filter(shopStocks -> getDateDifferenceInDays(expiryDateInterval, shopStocks.getExpiryDate()) <= 60)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShopStocks> allApprovedStock(Long shopId) {

        return this.allStockByShopId(shopId)
                .stream()
                .filter(ShopStocks::getApproved)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShopStocks> allUnApprovedStock(Long shopId) {

        return this.allStockByShopId(shopId)
                .stream()
                .filter(shopStocks -> !shopStocks.getApproved())
                .collect(Collectors.toList());
    }

    @Override
    public List<ShopStocks> allUnApprovedStockByCreator(String createdBy) {

        return shopStocksRepository.findAllByCreatedByAndApprovedIsFalse(createdBy);
    }

    @Override
    public ShopStocks approveStock(Long stockId) {

        if (AuthenticatedUserDetails.getAccount_type() == null) throw new InventoryAPIOperationException
                ("Unknown user", "Could not identify the type of user trying to view all stock in warehouse", null);

        if(AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)
                || AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT)) throw new
                InventoryAPIOperationException("Operation not allowed", "You are not authorized to add stocks" +
                " to a warehouse, contact the business owner or warehouse attendant", null);

        return shopStocksRepository.findById(stockId)
                .map(stockFound -> {

                    if (!stockFound.getShop().getCreatedBy()
                            .equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                        throw new InventoryAPIOperationException("Not allowed", "Stock not found in yor warehouse", null);

                    if (stockFound.getApproved()) throw new InventoryAPIOperationException("Stock already approved",
                            "Stock with id: " + stockId + " has already been approved by you", null);

                    stockFound.setUpdateDate(new Date());
                    stockFound.setApproved(true);
                    stockFound.setApprovedDate(new Date());
                    stockFound.setApprovedBy(AuthenticatedUserDetails.getUserFullName());

                    return shopStocksRepository.save(stockFound);
                }).orElse(null);
    }

    @Override
    public List<ShopStocks> approveStockList(List<Long> stockIdList) {

        return stockIdList.stream()
                .map(this::approveStock)
                .collect(Collectors.toList());
    }

    @Override
    public ShopStocks deleteStock(Long stockId) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) throw new
                InventoryAPIOperationException("Not allowed", "Operation not allowed for logged in user.", null);

        return shopStocksRepository.findById(stockId)
                .map(stockFound -> {

                    if (!stockFound.getShop().getCreatedBy()
                            .equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                        throw new InventoryAPIOperationException("Not allowed", "Stock not found in yor shop", null);

                    shopStocksRepository.delete(stockFound);
                    return stockFound;
                }).orElse(null);
    }

    @Transactional
    @Override
    public ShopStocks reStockToShop(Long shopId, Long stockId, ShopStocks newStock) {

        if(AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT)) throw new
                InventoryAPIOperationException("Operation not allowed", "You are not authorized to add stocks" +
                " to a warehouse, contact the business owner or warehouse attendant", null);

        if (null == shopId || shopId < 0 || !shopId.toString().matches("\\d+")
                || null == stockId || stockId < 0 || !stockId.toString().matches("\\d+")) throw new InventoryAPIOperationException
                ("warehouse id or stock id error", "warehouse id and/or stock id is empty or not a valid number", null);

        if (null == newStock) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find stock entity to save", null);

        return shopRepository.findById(shopId).map(shop -> {

            if (shop.getBusinessOwner().getHasWarehouse())throw new InventoryAPIOperationException
                    ("Operation not allowed", "You cannot add stock directly to this shop, you must" +
                            " first request a waybill from any of the business owner warehouses", null);

            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)
                    && !sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails
                    .getUserFullName()).getCreatedBy().equalsIgnoreCase(shop.getCreatedBy()))
                throw new InventoryAPIOperationException
                        ("Not your shop", "Warehouse does not belong to your creator", null);

            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)
                    && !shop.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Not your shop", "You cannot add stock to" +
                        " this shop because it was not created by you", null);

            Supplier stockSupplier = newStock.getLastRestockPurchasedFrom();

            stockSupplier = supplierRepository.findBySupplierPhoneNumber(stockSupplier.getSupplierPhoneNumber());

            if (null == stockSupplier) stockSupplier = genericService
                    .addSupplier(newStock.getLastRestockPurchasedFrom());

            Supplier finalStockSupplier = stockSupplier;

            Optional<ShopStocks> optionalShopStock = shopStocksRepository.findById(stockId);

            if (!optionalShopStock.isPresent()) throw new InventoryAPIOperationException
                    ("could not find an entity", "Could not find stock with the id " + stockId, null);

            return optionalShopStock.map(stock -> {

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
                stock.setStockPurchasedTotalPrice(newStock.getStockPurchasedTotalPrice().add(stock.getStockPurchasedTotalPrice()));
                stock.setStockRemainingTotalPrice(newStock.getStockPurchasedTotalPrice().add(stock.getStockRemainingTotalPrice()));
                stock.setPricePerStockPurchased(newStock.getStockPurchasedTotalPrice()
                        .divide(BigDecimal.valueOf(newStock.getStockQuantityPurchased()), 2));

                if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

                    stock.setApproved(true);
                    stock.setApprovedDate(new Date());
                    stock.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
                }
                return shopStocksRepository.save(stock);
            }).orElse(null);
        }).orElse(null);
    }

    @Override
    public ShopStocks addStockToShop(ShopStocks stockToAdd, Shop shop){

        if (null == stockToAdd) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find stock entity to save", null);

        Supplier stockSupplier = stockToAdd.getLastRestockPurchasedFrom();

        StockCategory stockCategory = stockToAdd.getStockCategory();

        stockCategory = stockCategoryRepository.findDistinctFirstByCategoryName(stockCategory.getCategoryName());

        if (null == stockCategory) stockCategory = genericService.addStockCategory(stockToAdd.getStockCategory());

        stockSupplier = supplierRepository
                .findBySupplierPhoneNumber(stockSupplier.getSupplierPhoneNumber());

        if (null == stockSupplier) stockSupplier = genericService.addSupplier(stockToAdd.getLastRestockPurchasedFrom());

        if (!StringUtils.isEmpty(stockToAdd.getStockBarCodeId())) {

            ShopStocks stockByBarcode = shopStocksRepository
                    .findDistinctByStockBarCodeId(stockToAdd.getStockBarCodeId());
            if (stockByBarcode != null
                    && !stockByBarcode.getStockName().equalsIgnoreCase(stockToAdd.getStockName())){

                throw new InventoryAPIDuplicateEntryException("Barcode already exist",
                        "Another stock exist with the barcode id you passed for the new stock you are about to add", null);
            }
        }

        ShopStocks existingStock = shopStocksRepository
                .findDistinctByStockNameAndShop(stockToAdd.getStockName(), shop);

        if (existingStock != null){

            existingStock.setLastRestockPurchasedFrom(stockSupplier);

            return reStockToShop(shop.getShopId(), existingStock.getShopStockId(), stockToAdd);
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
        stockToAdd.setShop(shop);
        stockToAdd.setLastRestockPurchasedFrom(stockSupplier);
        stockToAdd.setLastRestockQuantity(stockToAdd.getStockQuantityPurchased());
        stockToAdd.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
        stockToAdd.setLastRestockBy(AuthenticatedUserDetails.getUserFullName());
        stockToAdd.setStockQuantityRemaining(stockToAdd.getStockQuantityPurchased());
        stockToAdd.setStockRemainingTotalPrice(stockToAdd.getStockPurchasedTotalPrice());
        stockToAdd.setPricePerStockPurchased(stockToAdd.getStockPurchasedTotalPrice()
                .divide(BigDecimal.valueOf(stockToAdd.getStockQuantityPurchased()), 2));

        return shopStocksRepository.save(stockToAdd);
    }

    @Override
    public ShopStocks changeStockSellingPriceByStockId(Long stockId, BigDecimal newSellingPrice) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user cannot perform this operation", null);

        if (null == stockId || stockId < 0 || !stockId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("stock id error", "stock id is empty or not a valid number", null);

        if (null == newSellingPrice || is(newSellingPrice).lte(BigDecimal.ZERO) || !newSellingPrice.toString().matches("\\d+"))
            throw new InventoryAPIOperationException("selling price error", "selling price is empty or not a valid number", null);

        return shopStocksRepository.findById(stockId).map(stock -> {

            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)
                    && !stock.getShop().getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Not allowed",
                        "You cannot change selling price of a stock not found in your shop", null);

            if(AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)
                && !sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName()).getShop()
                    .equals(stock.getShop())) throw new InventoryAPIOperationException("Not allowed",
                    "You cannot change selling price of a stock not found in your shop", null);

            return shopStocksRepository.save(changeStockSellingPrice(stock, newSellingPrice));
        }).orElse(null);
    }

    @Override
    public ShopStocks changeStockSellingPriceByShopIdAndStockName(Long shopId, String stockName,
                                                                  BigDecimal newSellingPrice) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user cannot perform this operation", null);

        if (null == shopId || shopId < 0 || !shopId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("warehouse id error", "warehouse id is empty or not a valid number", null);

        if (null == stockName || stockName.isEmpty()) throw new
                InventoryAPIOperationException("stock name error", "stock name is empty or null", null);

        if (null == newSellingPrice || is(newSellingPrice).lte(BigDecimal.ZERO) || !newSellingPrice.toString().matches("\\d+"))
            throw new InventoryAPIOperationException("selling price error", "selling price is empty or not a valid number", null);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

            return shopRepository.findById(shopId).map(shop -> {

                if (!shop.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())) throw new
                        InventoryAPIOperationException("Operation not allowed",
                        "You cannot change sellig price of a stock in a shop not created by you", null);

                ShopStocks stockRetrieved = shopStocksRepository.findDistinctByStockNameAndShop(stockName, shop);

                if(stockRetrieved == null) throw new InventoryAPIResourceNotFoundException("Not found",
                        "Stock with name: " + stockName + " was not found in your shop", null);

                return shopStocksRepository.save(changeStockSellingPrice(stockRetrieved, newSellingPrice));

            }).orElse(null);
        }

        Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());

        ShopStocks stockRetrieved = shopStocksRepository.findDistinctByStockNameAndShop(stockName, seller.getShop());

        if(stockRetrieved == null) throw new InventoryAPIResourceNotFoundException("Not found",
                "Stock with name: " + stockName + " was not found in your shop", null);

        return shopStocksRepository.save(changeStockSellingPrice(stockRetrieved, newSellingPrice));
    }

    @Transactional
    @Override
    public Invoice sellStock(Long shopId, Invoice invoice) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Not allowed", "Operation not allowed by the logged in user", null);

        if (null == invoice) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find valid data to generate an invoice", null);

        Optional<Shop> optionalShop = shopRepository.findById(shopId);

        if (!optionalShop.isPresent()) throw new InventoryAPIOperationException("Shop not found",
                "Cannot find shop where sales is to be made from, review your inputs and try agin", null);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)
                && !optionalShop.get().getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "You cannot sell from a shop not created by you", null);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)
                && !optionalShop.get().equals(sellerRepository.findDistinctBySellerEmail
                (AuthenticatedUserDetails.getUserFullName()).getShop()))
            throw new InventoryAPIOperationException("Not allowed",
                    "You cannot sell from a shop you were not assigned", null);

        AtomicReference<Customer> customer = new AtomicReference<>();
        AtomicReference<ShopStocks> atomicStock = new AtomicReference<>();
        Set<StockSold> stockSoldSet = new HashSet<>();

        customer.set(customerRepository
                .findDistinctByCustomerPhoneNumber(invoice.getCustomerId().getCustomerPhoneNumber()));
        if (null == customer.get()) customer.set(genericService.addCustomer(invoice.getCustomerId()));

        BigDecimal totalToAmountPaidDiff = invoice.getInvoiceTotalAmount()
                .subtract(invoice.getAmountPaid().add(invoice.getDiscount()));
        if (is(totalToAmountPaidDiff).isPositive()) invoice.setDebt(totalToAmountPaidDiff.abs());


        invoice.setInvoiceNumber(UniqueIdentifier.invoiceUID());
        invoice.getStockSold().forEach(stockSold -> {

            atomicStock.set(shopStocksRepository
                    .findDistinctByStockNameAndShop(stockSold.getStockName(), optionalShop.get()));

            if (atomicStock.get() == null) return;

            ShopStocks stockFound = atomicStock.get();

            if (stockFound.getStockQuantityRemaining() < stockSold.getQuantitySold()) throw new InventoryAPIOperationException
                    ("Low stock quantity", "The quantity of " + stockFound.getStockName() +
                            " is limited, and you cannot sell above it.", null);

            stockSold.setCostPricePerStock(stockFound.getPricePerStockPurchased());
            stockSold.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
            stockSold.setStockSoldInvoiceId(invoice.getInvoiceNumber());
            stockSoldSet.add(stockSoldRepository.save(stockSold));

            stockFound.setStockQuantitySold(stockFound.getStockQuantitySold() + stockSold.getQuantitySold());
            stockFound.setStockSoldTotalPrice(stockFound.getStockSoldTotalPrice().add(stockSold.getCostPricePerStock()
                    .multiply(BigDecimal.valueOf(stockSold.getQuantitySold()))));
            stockFound.setStockQuantityRemaining(stockFound.getStockQuantityRemaining() - stockSold.getQuantitySold());
            stockFound.setStockRemainingTotalPrice(BigDecimal.valueOf(stockFound.getStockQuantityRemaining())
                    .multiply(stockFound.getPricePerStockPurchased()));
            stockFound.setProfit(stockFound.getStockSoldTotalPrice().subtract(stockFound.getStockPurchasedTotalPrice()));
            shopStocksRepository.save(stockFound);

            if (atomicStock.get() == null) throw new InventoryAPIResourceNotFoundException
                    ("Stock not found", "Stock with name " + stockSold.getStockName() + ", was not found in any of your warehouse", null);
        });

        String incomeDescription = "Income generated from sale of stock with invoice number: " + invoice.getInvoiceNumber();
        genericService.addIncome(new Income(invoice.getAmountPaid(), 100, incomeDescription));

        String invoiceGeneratedBy = AuthenticatedUserDetails.getUserFullName();

        invoice.getStockSold().clear();
        invoice.setStockSold(stockSoldSet);
        invoice.setCustomerId(customer.get());
        invoice.setCreatedBy(invoiceGeneratedBy);

        if (ACCOUNT_TYPE.SHOP_SELLER.equals(AuthenticatedUserDetails.getAccount_type()))
            invoice.setSeller(sellerRepository.findDistinctBySellerFullNameOrSellerEmail(invoiceGeneratedBy, invoiceGeneratedBy));

        return invoiceRepository.save(invoice);
    }

    @Transactional
    @Override
    public ReturnedStock processReturn(Long shopId, ReturnedStock returnedStock) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Not allowed", "Operation not allowed by the logged in user", null);

        if (returnedStock == null) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find returned stock entity to save", null);

        Invoice invoiceRetrieved = invoiceRepository.findDistinctByInvoiceNumber(returnedStock.getInvoiceId());
        if (null == invoiceRetrieved) throw new InventoryAPIResourceNotFoundException
                ("No invoice was found by id", "No invoice with id " + returnedStock.getInvoiceId() + " was found", null);

        ReturnedStock returnStock;

        ShopStocks stockRecordFromShop = shopRepository.findById(shopId)
                .map(shop -> {

                    if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)
                            && !shop.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                        throw new InventoryAPIOperationException("Operation not allowed",
                                "You cannot return sales to a shop not created by you", null);

                    if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)
                            && !shop.equals(sellerRepository.findDistinctBySellerEmail(
                                    AuthenticatedUserDetails.getUserFullName()).getShop()))
                        throw new InventoryAPIOperationException("Not allowed",
                                "You cannot return sales to a shop you were not assigned", null);

                    return shopStocksRepository
                            .findDistinctByStockNameAndShop(returnedStock.getStockName(), shop);
                }).orElse(null);

        if (null == stockRecordFromShop) throw new InventoryAPIResourceNotFoundException
                ("Stock not found", "The stock about to be returned never existed in any of your shop", null);

        StockSold stockAboutToBeReturned = invoiceRetrieved.getStockSold()
                .stream()
                .filter(stockSold -> stockSold.getStockName().equalsIgnoreCase(returnedStock.getStockName()))
                .collect(toSingleton());

        if (null == stockAboutToBeReturned) throw new InventoryAPIOperationException("Stock does not exist in the invoice",
                "The stock about to be returned does not exist in the list of stocks sold with the invoice", null);

        if (stockAboutToBeReturned.getQuantitySold() < returnedStock.getQuantityReturned()) throw new InventoryAPIOperationException
                ("Quantity returned is invalid", "Quantity returned is above quantity sold, review your inputs", null);

        returnedStock.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
        returnedStock.setCustomerId(invoiceRetrieved.getCustomerId());
        returnedStock.setStockReturnedCost(BigDecimal.valueOf(returnedStock.getQuantityReturned())
                .multiply(stockRecordFromShop.getSellingPricePerStock()));

        stockRecordFromShop.setStockRemainingTotalPrice(stockRecordFromShop.getStockRemainingTotalPrice()
                .add(BigDecimal.valueOf(returnedStock.getQuantityReturned())
                        .multiply(stockAboutToBeReturned.getCostPricePerStock())));
        stockRecordFromShop.setStockQuantityRemaining(returnedStock.getQuantityReturned() +
                stockRecordFromShop.getStockQuantityRemaining());
        stockRecordFromShop.setProfit(stockRecordFromShop.getProfit()
                .subtract(BigDecimal.valueOf(returnedStock.getQuantityReturned())
                        .multiply(stockRecordFromShop.getSellingPricePerStock())));
        stockRecordFromShop.setStockSoldTotalPrice(stockRecordFromShop.getStockSoldTotalPrice()
                .subtract(BigDecimal.valueOf(returnedStock.getQuantityReturned())
                        .multiply(stockAboutToBeReturned.getPricePerStockSold())));
        stockRecordFromShop.setStockQuantitySold(stockRecordFromShop.getStockQuantitySold() -
                returnedStock.getQuantityReturned());
        stockRecordFromShop.setUpdateDate(new Date());

        shopStocksRepository.save(stockRecordFromShop);

        StockSold initStockSold = stockSoldRepository.findDistinctByStockSoldInvoiceIdAndStockName
                (returnedStock.getInvoiceId(), stockAboutToBeReturned.getStockName());
        initStockSold.setUpdateDate(new Date());
        initStockSold.setQuantitySold(initStockSold.getQuantitySold() - returnedStock.getQuantityReturned());

        Set<StockSold> stockSoldSet = new HashSet<>(invoiceRetrieved.getStockSold());

        if (initStockSold.getQuantitySold() > 0){

            stockSoldSet.remove(stockAboutToBeReturned);
            stockSoldSet.add(initStockSold);
            invoiceRetrieved.setStockSold(stockSoldSet);
            invoiceRetrieved.setPaymentModeVal(String.valueOf(invoiceRetrieved.getPaymentModeValue()));
            invoiceRepository.save(invoiceRetrieved);
        }
        else{

            if (invoiceRetrieved.getStockSold().size() == 1){

                invoiceRepository.delete(invoiceRetrieved);
            }else {

                stockSoldSet.remove(stockAboutToBeReturned);
                stockSoldRepository.delete(initStockSold);
                invoiceRetrieved.setStockSold(stockSoldSet);
                invoiceRetrieved.setPaymentModeVal(String.valueOf(invoiceRetrieved.getPaymentModeValue()));
                invoiceRepository.save(invoiceRetrieved);
            }
        }

        String expenseDescription = returnedStock.getStockName() + " returned with reason: " + returnedStock.getReasonForReturn();
        Expense expenseOnReturn = new Expense(300, returnedStock.getStockReturnedCost(), expenseDescription);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)) {

            Shop stockReturnedShop = genericService.shopBySellerName(AuthenticatedUserDetails.getUserFullName());
            returnedStock.setShop(stockReturnedShop);

            genericService.addExpense(expenseOnReturn);
            returnStock = returnedStockRepository.save(returnedStock);
        }else{

            returnedStock.setApproved(true);
            returnedStock.setApprovedDate(new Date());
            returnedStock.setApprovedBy(AuthenticatedUserDetails.getUserFullName());

            genericService.addExpense(expenseOnReturn);
            returnStock = returnedStockRepository.save(returnedStock);
        }

        return returnStock;
    }

    @Override
    public List<ReturnedStock> processReturnList(Long shopId, List<ReturnedStock> returnedStockList) {

        if (null == returnedStockList || returnedStockList.isEmpty()) throw new InventoryAPIOperationException
                ("Invalid list of returned stock", "Returned stock list is empty or null", null);

        return returnedStockList.stream()
                .map(returnedStock -> this.processReturn(shopId, returnedStock))
                .collect(Collectors.toList());
    }

    private ShopStocks changeStockSellingPrice(ShopStocks stock, BigDecimal newSellingPrice) {

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
