package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model.ResponseObject;
import com.chrisworks.personal.inventorysystem.Backend.Entities.BulkUploadResponseWrapper;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.Entities.UniqueStock;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDuplicateEntryException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.*;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.chrisworks.personal.inventorysystem.Backend.Utility.UniqueIdentifier;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices.UserMiniProfileRepository;
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
 * @since 12/26/2019
 * @email chriseteka@gmail.com
 */
@Service
public class ShopStockServicesImpl implements ShopStockServices {

    private final SellerRepository sellerRepository;

    private final ShopStocksRepository shopStocksRepository;

    private final ShopRepository shopRepository;

    private final GenericService genericService;

    private final InvoiceServices invoiceServices;

    private final StockSoldRepository stockSoldRepository;

    private final ReturnedStockRepository returnedStockRepository;

    private final StockCategoryRepository stockCategoryRepository;

    private final SupplierRepository supplierRepository;

    private final LoyaltyRepository loyaltyRepository;

    private final CustomerRepository customerRepository;

    private final SalesDiscountServices salesDiscountServices;

    private final ExchangedStockRepository exchangedStockRepository;

    private final IncomeServices incomeServices;

    private final ExpenseServices expenseServices;

    private final UserMiniProfileRepository userMiniProfileRepository;

    @Autowired
    public ShopStockServicesImpl(SellerRepository sellerRepository, ShopStocksRepository shopStocksRepository,
                                 ShopRepository shopRepository, GenericService genericService,
                                 InvoiceServices invoiceServices, StockSoldRepository stockSoldRepository,
                                 ReturnedStockRepository returnedStockRepository, SupplierRepository supplierRepository,
                                 StockCategoryRepository stockCategoryRepository, LoyaltyRepository loyaltyRepository,
                                 CustomerRepository customerRepository, SalesDiscountServices salesDiscountServices,
                                 ExchangedStockRepository exchangedStockRepository, IncomeServices incomeServices,
                                 ExpenseServices expenseServices, UserMiniProfileRepository userMiniProfileRepository) {
        this.sellerRepository = sellerRepository;
        this.shopStocksRepository = shopStocksRepository;
        this.shopRepository = shopRepository;
        this.genericService = genericService;
        this.invoiceServices = invoiceServices;
        this.stockSoldRepository = stockSoldRepository;
        this.returnedStockRepository = returnedStockRepository;
        this.stockCategoryRepository = stockCategoryRepository;
        this.supplierRepository = supplierRepository;
        this.loyaltyRepository = loyaltyRepository;
        this.customerRepository = customerRepository;
        this.salesDiscountServices = salesDiscountServices;
        this.exchangedStockRepository = exchangedStockRepository;
        this.incomeServices = incomeServices;
        this.expenseServices = expenseServices;
        this.userMiniProfileRepository = userMiniProfileRepository;
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
        }).orElseThrow(()-> new InventoryAPIOperationException("Shop not found",
                "Shop with id: " + shopId + " was not found.", null));
    }

    @Override
    @Transactional
    public BulkUploadResponseWrapper createStockListInShop(Long shopId, List<ShopStocks> stocksList) {

        if (AuthenticatedUserDetails.getAccount_type() == null) throw new InventoryAPIOperationException
                ("Unknown user", "Could not identify the type of user trying to add stock to shop", null);

        return shopRepository.findById(shopId).map(shop -> {

            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)){

                return sellerRepository.findById(AuthenticatedUserDetails.getUserId())
                    .map(seller -> {

                        if (!seller.getCreatedBy().equalsIgnoreCase(shop.getCreatedBy())) throw new
                                InventoryAPIOperationException
                                ("Not your Shop", "Shop does not belong to your creator", null);

                        if (shop.getBusinessOwner().getHasWarehouse())throw new InventoryAPIOperationException
                                ("Operation not allowed", "You cannot add stock directly to this shop, you must" +
                                        " first request a waybill from any of the business owner warehouses", null);

                        return bulkUploadToShopStock(stocksList, shop);
                    }).orElseThrow(() -> new InventoryAPIResourceNotFoundException("Seller not found",
                                "Could not determine the seller making this request", null));
            }
            else if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

                if (!shop.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())) throw new
                        InventoryAPIOperationException("Not your Shop", "Shop you are about to add a stock" +
                        " does not belong to you, cannot proceed with this operation", null);

                if (AuthenticatedUserDetails.getHasWarehouse()) throw new InventoryAPIOperationException
                        ("Operation not allowed", "You cannot add stock directly to this shop, you must" +
                                " first request a waybill from any of the business owner warehouses", null);

                return bulkUploadToShopStock(stocksList, shop);
            }
            else throw new InventoryAPIOperationException
                        ("Operation not allowed", "User attempting this operation is not allowed to proceed", null);
        }).orElseThrow(()-> new InventoryAPIOperationException("Shop not found",
                "Shop with id: " + shopId + " was not found.", null));
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
                }).orElseThrow(() -> new InventoryAPIResourceNotFoundException("Shop not found",
                        "Shop with id: " + shopId + " was not found", null));
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
    public List<ShopStocks> deleteStock(Long shopId, Long... stockId) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) throw new
                InventoryAPIOperationException("Not allowed", "Operation not allowed for logged in user.", null);

        List<Long> stockIdsToBeDeleted = Arrays.asList(stockId);
        List<ShopStocks> shopStocks = allStockByShopId(shopId).stream()
                .filter(s -> stockIdsToBeDeleted.contains(s.getShopStockId()))
                .collect(Collectors.toList());

        if (!shopStocks.isEmpty()) shopStocksRepository.deleteAll(shopStocks);

        return shopStocks;
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

            if (!shop.getBusinessOwner().getHasWarehouse())throw new InventoryAPIOperationException
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

            Supplier finalStockSupplier = genericService.addSupplier(newStock.getLastRestockPurchasedFrom());

            Optional<ShopStocks> optionalShopStock = shopStocksRepository.findById(stockId);

            if (!optionalShopStock.isPresent()) throw new InventoryAPIOperationException
                    ("could not find an entity", "Could not find stock with the id " + stockId, null);

            return optionalShopStock.map(stock -> {

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
                newStock.setStockPurchasedTotalPrice(newStock.getPricePerStockPurchased()
                        .multiply(BigDecimal.valueOf(newStock.getStockQuantityPurchased())));
                stock.setStockPurchasedTotalPrice(newStock.getStockPurchasedTotalPrice().add(stock.getStockPurchasedTotalPrice()));
                stock.setStockRemainingTotalPrice(newStock.getStockPurchasedTotalPrice().add(stock.getStockRemainingTotalPrice()));

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

        Supplier stockSupplier = genericService.addSupplier(stockToAdd.getLastRestockPurchasedFrom());

        StockCategory stockCategory = genericService.addStockCategory(stockToAdd.getStockCategory());

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
        stockToAdd.setStockPurchasedTotalPrice(stockToAdd.getPricePerStockPurchased()
                .multiply(BigDecimal.valueOf(stockToAdd.getStockQuantityPurchased())));
        stockToAdd.setStockRemainingTotalPrice(stockToAdd.getStockPurchasedTotalPrice());

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
                        "You cannot change selling price of a stock in a shop not created by you", null);

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
                "Cannot find shop where sales is to be made from, review your inputs and try again", null);

        Shop shop = optionalShop.get();

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)
                && !shop.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "You cannot sell from a shop not created by you", null);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)
                && !shop.equals(sellerRepository.findDistinctBySellerEmail
                (AuthenticatedUserDetails.getUserFullName()).getShop()))
            throw new InventoryAPIOperationException("Not allowed",
                    "You cannot sell from a shop you were not assigned", null);

        AtomicReference<Customer> customer = new AtomicReference<>();
        AtomicReference<ShopStocks> atomicStock = new AtomicReference<>();
        Set<StockSold> stockSoldSet = new HashSet<>();

        if (invoice.getCustomerId() != null
                && invoice.getCustomerId().getCustomerPhoneNumber() != null
                && invoice.getCustomerId().getCustomerFullName() != null)
            customer.set(genericService.addCustomer(invoice.getCustomerId()));

        //Process the incoming invoice to take note of any promo that maybe attached
        final Invoice preProcessedInvoice = genericService.processPromoIfExist(invoice);

        BigDecimal totalToAmountPaidDiff = preProcessedInvoice.getInvoiceTotalAmount()
                .subtract(preProcessedInvoice.getAmountPaid()
                        .add(preProcessedInvoice.getDiscount())
                        .add(preProcessedInvoice.getLoyaltyDiscount()));
        if (is(totalToAmountPaidDiff).isPositive()) preProcessedInvoice.setDebt(totalToAmountPaidDiff.abs());
        else preProcessedInvoice.setBalance(totalToAmountPaidDiff.abs());

        preProcessedInvoice.setInvoiceNumber(UniqueIdentifier.invoiceUID());
        preProcessedInvoice.getStockSold().forEach(stockSold -> {

            atomicStock.set(shopStocksRepository
                    .findDistinctByStockNameAndShop(stockSold.getStockName(), shop));

            if (atomicStock.get() == null) return;

            ShopStocks stockFound = atomicStock.get();

            if (stockFound.getStockQuantityRemaining() == 0)
                throw new InventoryAPIOperationException("Low stock quantity",
                        stockFound.getStockName() + " has finished in your shop, remove it from your invoice.", null);

            if (stockFound.getStockQuantityRemaining() < stockSold.getQuantitySold()) throw new InventoryAPIOperationException
                    ("Low stock quantity", "The quantity of " + stockFound.getStockName() +
                            " is limited, and you cannot sell above it.", null);

            stockSold.setCostPricePerStock(stockFound.getPricePerStockPurchased());
            stockSold.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
            stockSold.setStockSoldInvoiceId(preProcessedInvoice.getInvoiceNumber());
            stockSoldSet.add(stockSoldRepository.save(stockSold));

            //Generate discount on this stock sold if it exists
            salesDiscountServices.generateDiscountOnStockSold(stockSold, stockFound.getSellingPricePerStock(),
                    preProcessedInvoice.getInvoiceNumber(), preProcessedInvoice.getCustomerId().getCustomerFullName());

            stockFound.setStockQuantitySold(stockFound.getStockQuantitySold() + stockSold.getQuantitySold());
            stockFound.setStockSoldTotalPrice(stockFound.getStockSoldTotalPrice().add(stockSold.getPricePerStockSold()
                    .multiply(BigDecimal.valueOf(stockSold.getQuantitySold()))));
            stockFound.setStockQuantityRemaining(stockFound.getStockQuantityRemaining() - stockSold.getQuantitySold());
            stockFound.setStockRemainingTotalPrice(BigDecimal.valueOf(stockFound.getStockQuantityRemaining())
                    .multiply(stockFound.getPricePerStockPurchased()));
            stockFound.setProfit(stockFound.getProfit().add(
                    (stockSold.getPricePerStockSold().subtract(stockSold.getCostPricePerStock()))
                    .multiply(BigDecimal.valueOf(stockSold.getQuantitySold()))
            ));
            shopStocksRepository.save(stockFound);

            if (atomicStock.get() == null) throw new InventoryAPIResourceNotFoundException
                    ("Stock not found", "Stock with name " + stockSold.getStockName() + ", was not found in any of your warehouse", null);
        });

        if (is(preProcessedInvoice.getAmountPaid()).isPositive()) {

            String incomeDescription = "Income generated from sale of stock with invoice number: " + preProcessedInvoice.getInvoiceNumber();

            if (preProcessedInvoice.getMultiplePayment().isEmpty())
                incomeServices.createEntity(new Income(preProcessedInvoice.getAmountPaid(), 100, incomeDescription));
            else {
                preProcessedInvoice.setPaymentModeValue(400);
                for (MultiplePaymentMode multiplePaymentMode : preProcessedInvoice.getMultiplePayment())
                    incomeServices.createEntity(new Income(multiplePaymentMode.getAmountPaid(), 100, incomeDescription));
            }
        }

        String invoiceGeneratedBy = AuthenticatedUserDetails.getUserFullName();

        Customer cust = customer.get();
        preProcessedInvoice.getStockSold().clear();
        preProcessedInvoice.setStockSold(stockSoldSet);
        preProcessedInvoice.setShop(shop);
        preProcessedInvoice.setSoldBy(userMiniProfileRepository.findDistinctByEmail(invoiceGeneratedBy));
        if (cust != null && cust.getIsLoyal()){

            Loyalty loyaltyPlan = loyaltyRepository.findDistinctByCustomers(cust);
            if (null != loyaltyPlan){
                if (is(preProcessedInvoice.getLoyaltyDiscount()).lte(0.0)) {

                    cust.setRecentPurchasesAmount(cust.getRecentPurchasesAmount().add(preProcessedInvoice.getAmountPaid()));
                    cust.setNumberOfPurchasesAfterLastReward(cust.getNumberOfPurchasesAfterLastReward() + 1);
                    customer.set(customerRepository.save(cust));
                }
                else {

                    cust.setNumberOfPurchasesAfterLastReward(0);
                    cust.setRecentPurchasesAmount(BigDecimal.ZERO);
                    customer.set(customerRepository.save(cust));

                    //Generate discount on invoice if it exists
                    salesDiscountServices.generateDiscountOnLoyalCustomers(customer.get(), preProcessedInvoice);
                }
            }
        }

        if (preProcessedInvoice.getCustomerId() != null && preProcessedInvoice.getCustomerId().getCustomerPhoneNumber() != null)
            preProcessedInvoice.setCustomerId(customer.get());

        //Generate discount on invoice if it exists
        if (is(preProcessedInvoice.getDiscount()).isPositive())
            salesDiscountServices.generateDiscountOnInvoice(preProcessedInvoice);
        preProcessedInvoice.setCreatedBy(invoiceGeneratedBy);

        if (ACCOUNT_TYPE.SHOP_SELLER.equals(AuthenticatedUserDetails.getAccount_type()))
            preProcessedInvoice.setSeller(sellerRepository.findDistinctBySellerFullNameOrSellerEmail(invoiceGeneratedBy, invoiceGeneratedBy));

        return invoiceServices.createEntity(preProcessedInvoice);
    }

    @Transactional
    @Override
    public ResponseObject reverseSale(Long shopId, String invoiceNumber) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Not allowed", "Operation not allowed by the logged in user", null);

        Optional<Shop> optionalShop = shopRepository.findById(shopId);

        if (!optionalShop.isPresent()) throw new InventoryAPIOperationException("Shop not found",
                "Cannot find shop where sales is to be made from, review your inputs and try again", null);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)
                && !optionalShop.get().getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "You cannot sell from a shop not created by you", null);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)
                && !optionalShop.get().equals(sellerRepository.findDistinctBySellerEmail
                (AuthenticatedUserDetails.getUserFullName()).getShop()))
            throw new InventoryAPIOperationException("Not allowed",
                    "You cannot sell from a shop you were not assigned", null);

        Invoice invoice = invoiceServices.fetchInvoiceByInvoiceNumber(invoiceNumber);

        if (null == invoice) throw new InventoryAPIResourceNotFoundException("Invoice not found",
                "Invoice with number " + invoiceNumber + " was not found, review your inputs and try again", null);

        List<ReturnedStock> successfulReturns = new ArrayList<>(Collections.emptyList());

        invoice.getStockSold().forEach(stockSold -> {
            ReturnedStock returnedStock = new ReturnedStock();
            returnedStock.setReasonForReturn("Sale reversal on invoice number: " + invoiceNumber);
            returnedStock.setStockName(stockSold.getStockName());
            returnedStock.setQuantityReturned(stockSold.getQuantitySold());
            returnedStock.setInvoiceId(invoiceNumber);
            successfulReturns.add(this.processReturn(shopId, returnedStock));
        });

        if (successfulReturns.size() == invoice.getStockSold().size() && revertCashFlowOnSaleReversal(invoiceNumber))
            return new ResponseObject(true, "Sales reversal completed successfully");
        else return new ResponseObject(false, "Sales reversal was not successful, try again later.");
    }

    @Transactional
    @Override
    public ReturnedStock processReturn(Long shopId, ReturnedStock returnedStock) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Not allowed", "Operation not allowed by the logged in user", null);

        if (returnedStock == null) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find returned stock entity to save", null);

        Invoice invoiceRetrieved = invoiceServices.fetchInvoiceByInvoiceNumber(returnedStock.getInvoiceId());
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
                }).orElseThrow(()-> new InventoryAPIOperationException("Shop not found",
                        "Shop with id: " + shopId + " was not found.", null));

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

        returnedStock.setStockReturnedCost(BigDecimal.valueOf(returnedStock.getQuantityReturned())
                .multiply(stockRecordFromShop.getSellingPricePerStock()));
        returnedStock.setCreatedBy(AuthenticatedUserDetails.getUserFullName());

        Customer cust = invoiceRetrieved.getCustomerId();
        int sizeOfStockSold = invoiceRetrieved.getStockSold().size();
        if (cust != null){

            if (is(cust.getRecentPurchasesAmount()).isPositive() && cust.getIsLoyal()){
                cust.setRecentPurchasesAmount(cust.getRecentPurchasesAmount()
                        .subtract(returnedStock.getStockReturnedCost()));
                if (sizeOfStockSold == 1)
                    cust.setNumberOfPurchasesAfterLastReward(cust.getNumberOfPurchasesAfterLastReward() - 1);
                cust = customerRepository.save(cust);
            }
            returnedStock.setCustomerId(cust);
        }

        stockRecordFromShop.setStockRemainingTotalPrice(stockRecordFromShop.getStockRemainingTotalPrice()
                .add(BigDecimal.valueOf(returnedStock.getQuantityReturned())
                        .multiply(stockAboutToBeReturned.getCostPricePerStock())));
        stockRecordFromShop.setStockQuantityRemaining(returnedStock.getQuantityReturned() +
                stockRecordFromShop.getStockQuantityRemaining());
        stockRecordFromShop.setProfit(stockRecordFromShop.getProfit()
                .subtract(BigDecimal.valueOf(returnedStock.getQuantityReturned())
                        .multiply(stockAboutToBeReturned.getPricePerStockSold()
                                .subtract(stockAboutToBeReturned.getCostPricePerStock()))));
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
            invoiceServices.updateEntity(invoiceRetrieved.getInvoiceId(), invoiceRetrieved);
        }
        else{

            if (sizeOfStockSold == 1){

                invoiceServices.deleteEntity(invoiceRetrieved.getInvoiceId());
            }else {

                stockSoldSet.remove(stockAboutToBeReturned);
                initStockSold.setQuantitySold(1);
                stockSoldRepository.delete(initStockSold);
                invoiceRetrieved.setStockSold(stockSoldSet);
                invoiceRetrieved.setPaymentModeVal(String.valueOf(invoiceRetrieved.getPaymentModeValue()));
                invoiceServices.updateEntity(invoiceRetrieved.getInvoiceId(), invoiceRetrieved);
            }
        }

        String expenseDescription = returnedStock.getStockName() + " returned with reason: " + returnedStock.getReasonForReturn();
        Expense expenseOnReturn = new Expense(300, returnedStock.getStockReturnedCost(), expenseDescription);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)) {

            Shop stockReturnedShop = genericService.shopBySellerName(AuthenticatedUserDetails.getUserFullName());
            returnedStock.setShop(stockReturnedShop);

            expenseServices.createEntity(expenseOnReturn);
            returnStock = returnedStockRepository.save(returnedStock);
        }else{

            returnedStock.setApproved(true);
            returnedStock.setApprovedDate(new Date());
            returnedStock.setApprovedBy(AuthenticatedUserDetails.getUserFullName());

            expenseServices.createEntity(expenseOnReturn);
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

    @Override
    @Transactional
    public Invoice processExchange(Long shopId, ReturnedStock returnedStock, ExchangedStock receivedStock) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Not allowed", "Operation not allowed by the logged in user", null);

        if (returnedStock == null || receivedStock == null) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find returned or/and received stock complete exchange", null);

        Invoice invoiceRetrieved = invoiceServices.fetchInvoiceByInvoiceNumber(returnedStock.getInvoiceId());
        if (null == invoiceRetrieved) throw new InventoryAPIResourceNotFoundException
                ("No invoice was found by id", "No invoice with id " + returnedStock.getInvoiceId() + " was found", null);

        Shop shopInAction = shopRepository.findById(shopId)
            .map(shop -> {

                if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)
                        && !shop.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                    throw new InventoryAPIOperationException("Operation not allowed",
                            "You cannot process exchange in a shop that is not created by you", null);

                if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)
                        && !shop.equals(sellerRepository.findDistinctBySellerEmail(
                        AuthenticatedUserDetails.getUserFullName()).getShop()))
                    throw new InventoryAPIOperationException("Not allowed",
                            "You cannot process exchange in a shop you were not assigned", null);

                return shop;
            }).orElseThrow(() -> new InventoryAPIOperationException("Shop not found",
                    "Shop with id: " + shopId + " was not found.", null));

        ShopStocks returnedStockRecordInShop = shopStocksRepository
                .findDistinctByStockNameAndShop(returnedStock.getStockName(), shopInAction);

        ShopStocks exchangedStockRecordInShop = shopStocksRepository
                .findDistinctByStockNameAndShop(receivedStock.getStockName(), shopInAction);

        if (null == returnedStockRecordInShop) throw new InventoryAPIResourceNotFoundException
                ("Stock not found", "The stock about to be returned never existed in this shop", null);

        StockSold stockAboutToBeReturned = invoiceRetrieved.getStockSold()
                .stream()
                .filter(stockSold -> stockSold.getStockName().equalsIgnoreCase(returnedStock.getStockName()))
                .collect(toSingleton());

        if (stockAboutToBeReturned.getQuantitySold() < returnedStock.getQuantityReturned()) throw new InventoryAPIOperationException
                ("Quantity returned is invalid", "Quantity returned is above quantity sold, review your inputs", null);

        if (null == exchangedStockRecordInShop) throw new InventoryAPIResourceNotFoundException
                ("Stock not found", "The stock about to be exchanged does not exist in this shop", null);

        if (exchangedStockRecordInShop.getStockQuantityRemaining() < receivedStock.getQuantityReceived()) throw new InventoryAPIOperationException
                ("Quantity returned is invalid", "Quantity about to be exchanged is above the total number of stock in the shop", null);

        returnedStock.setStockReturnedCost(BigDecimal.valueOf(returnedStock.getQuantityReturned())
                .multiply(returnedStockRecordInShop.getSellingPricePerStock()));
        returnedStock.setCreatedBy(AuthenticatedUserDetails.getUserFullName());

        receivedStock.setStockReceivedCost(BigDecimal.valueOf(receivedStock.getQuantityReceived())
                .multiply(exchangedStockRecordInShop.getSellingPricePerStock()));
        returnedStock.setCreatedBy(AuthenticatedUserDetails.getUserFullName());

        Customer cust = invoiceRetrieved.getCustomerId();
        int sizeOfStockSold = invoiceRetrieved.getStockSold().size();
        BigDecimal stockReceivedCost = receivedStock.getStockReceivedCost();
        BigDecimal stockReturnedCost = returnedStock.getStockReturnedCost();
        if (cust != null){

            if (is(cust.getRecentPurchasesAmount()).isPositive() && cust.getIsLoyal()){
                cust.setRecentPurchasesAmount((cust.getRecentPurchasesAmount()
                        .subtract(stockReturnedCost))
                        .add(stockReceivedCost));
                if (sizeOfStockSold == 1)
                    cust.setNumberOfPurchasesAfterLastReward(cust.getNumberOfPurchasesAfterLastReward() - 1);
                cust = customerRepository.save(cust);
            }
            returnedStock.setCustomerId(cust);
            receivedStock.setCustomerId(cust);
        }

        returnedStockRecordInShop.setStockRemainingTotalPrice(returnedStockRecordInShop.getStockRemainingTotalPrice()
                .add(BigDecimal.valueOf(returnedStock.getQuantityReturned())
                        .multiply(stockAboutToBeReturned.getCostPricePerStock())));
        returnedStockRecordInShop.setStockQuantityRemaining(returnedStock.getQuantityReturned() +
                returnedStockRecordInShop.getStockQuantityRemaining());
        returnedStockRecordInShop.setProfit(returnedStockRecordInShop.getProfit()
                .subtract(BigDecimal.valueOf(returnedStock.getQuantityReturned())
                        .multiply(stockAboutToBeReturned.getPricePerStockSold()
                                .subtract(stockAboutToBeReturned.getCostPricePerStock()))));
        returnedStockRecordInShop.setStockSoldTotalPrice(returnedStockRecordInShop.getStockSoldTotalPrice()
                .subtract(BigDecimal.valueOf(returnedStock.getQuantityReturned())
                        .multiply(stockAboutToBeReturned.getPricePerStockSold())));
        returnedStockRecordInShop.setStockQuantitySold(returnedStockRecordInShop.getStockQuantitySold() -
                returnedStock.getQuantityReturned());
        returnedStockRecordInShop.setUpdateDate(new Date());

        exchangedStockRecordInShop.setStockRemainingTotalPrice(exchangedStockRecordInShop.getStockRemainingTotalPrice()
                .subtract(BigDecimal.valueOf(receivedStock.getQuantityReceived())
                        .multiply(exchangedStockRecordInShop.getSellingPricePerStock())));
        exchangedStockRecordInShop.setStockQuantityRemaining(exchangedStockRecordInShop.getStockQuantityRemaining()
                - receivedStock.getQuantityReceived());
        exchangedStockRecordInShop.setProfit(exchangedStockRecordInShop.getProfit()
                .add(BigDecimal.valueOf(receivedStock.getQuantityReceived())
                        .multiply(exchangedStockRecordInShop.getSellingPricePerStock()
                                .subtract(exchangedStockRecordInShop.getPricePerStockPurchased()))));
        exchangedStockRecordInShop.setStockSoldTotalPrice(exchangedStockRecordInShop.getStockSoldTotalPrice()
                .add(BigDecimal.valueOf(receivedStock.getQuantityReceived())
                        .multiply(exchangedStockRecordInShop.getSellingPricePerStock())));
        exchangedStockRecordInShop.setStockQuantitySold(exchangedStockRecordInShop.getStockQuantitySold() +
                receivedStock.getQuantityReceived());
        exchangedStockRecordInShop.setUpdateDate(new Date());

        shopStocksRepository.save(returnedStockRecordInShop);
        shopStocksRepository.save(exchangedStockRecordInShop);

        StockSold initStockSold = stockSoldRepository.findDistinctByStockSoldInvoiceIdAndStockName
                (returnedStock.getInvoiceId(), stockAboutToBeReturned.getStockName());
        initStockSold.setUpdateDate(new Date());
        initStockSold.setQuantitySold(initStockSold.getQuantitySold() - returnedStock.getQuantityReturned());

        StockSold newStockSoldFromExchange = new StockSold();

        newStockSoldFromExchange.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
        newStockSoldFromExchange.setQuantitySold(receivedStock.getQuantityReceived());
        newStockSoldFromExchange.setStockSoldInvoiceId(invoiceRetrieved.getInvoiceNumber());
        newStockSoldFromExchange.setCostPricePerStock(exchangedStockRecordInShop.getPricePerStockPurchased());
        newStockSoldFromExchange.setPricePerStockSold(exchangedStockRecordInShop.getSellingPricePerStock());
        newStockSoldFromExchange.setStockCategory(exchangedStockRecordInShop.getStockCategory().getCategoryName());
        newStockSoldFromExchange.setStockName(exchangedStockRecordInShop.getStockName());

        StockSold savedNewStockSold = stockSoldRepository.save(newStockSoldFromExchange);

        Set<StockSold> stockSoldSet = new HashSet<>(invoiceRetrieved.getStockSold());

        stockSoldSet.add(savedNewStockSold);
        invoiceRetrieved.setStockSold(stockSoldSet);

        if (initStockSold.getQuantitySold() > 0){

            stockSoldSet.remove(stockAboutToBeReturned);
            stockSoldSet.add(initStockSold);
            invoiceRetrieved.setStockSold(stockSoldSet);
            invoiceRetrieved.setPaymentModeVal(String.valueOf(invoiceRetrieved.getPaymentModeValue()));
        }
        else{

            if (sizeOfStockSold == 1){

                invoiceServices.deleteEntity(invoiceRetrieved.getInvoiceId());
            }else {

                stockSoldSet.remove(stockAboutToBeReturned);
                stockSoldRepository.delete(initStockSold);
                invoiceRetrieved.setStockSold(stockSoldSet);
                invoiceRetrieved.setPaymentModeVal(String.valueOf(invoiceRetrieved.getPaymentModeValue()));
            }
        }

        String status;
        if (is(stockReceivedCost).eq(stockReturnedCost)) {
            status = "Returned stock cost equals cost of stock exchanged with it";
            invoiceRetrieved.setBalance(BigDecimal.ZERO);
        }
        else if (is(stockReceivedCost).gt(stockReturnedCost)) {
            status = "Returned stock cost less than stock exchanged with it, customer is to pay: "
                    + stockReceivedCost.subtract(stockReturnedCost);
            invoiceRetrieved.setDebt(invoiceRetrieved.getDebt()
                    .add(stockReceivedCost
                    .subtract(stockReturnedCost)));
        }
        else{
            status = "Returned stock cost more than stock exchanged with it, customer is to be balanced: "
                    + stockReturnedCost.subtract(stockReceivedCost);
            invoiceRetrieved.setBalance(stockReturnedCost.subtract(stockReceivedCost));

            String expenseDescription = returnedStock.getStockName() + " exchanged with: " + receivedStock.getStockName()
            + " with reason: " + returnedStock.getReasonForReturn();
            Expense expenseOnReturn = new Expense(300, returnedStock.getStockReturnedCost(), expenseDescription);
            expenseServices.createEntity(expenseOnReturn);
        }

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)) {

            Shop stockExchangedShop = genericService.shopBySellerName(AuthenticatedUserDetails.getUserFullName());
            returnedStock.setShop(stockExchangedShop);
            receivedStock.setShop(stockExchangedShop);
        }else{

            returnedStock.setApproved(true);
            returnedStock.setApprovedDate(new Date());
            returnedStock.setApprovedBy(AuthenticatedUserDetails.getUserFullName());

            receivedStock.setApproved(true);
            receivedStock.setApprovedDate(new Date());
            receivedStock.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
        }

        ReturnedStock savedReturnedStock = returnedStockRepository.save(returnedStock);
        ExchangedStock savedExchangedStock = exchangedStockRepository.save(receivedStock);

        Invoice updatedInvoice = invoiceServices.updateEntity(invoiceRetrieved.getInvoiceId(), invoiceRetrieved);

        if (savedReturnedStock == null || savedExchangedStock == null || updatedInvoice == null)
            throw new InventoryAPIOperationException("Exchange incomplete",
                    "Exchange not completed successfully, review your inputs and try again.", null);

        updatedInvoice.setNotice(status);
        return updatedInvoice;
    }

    @Override
    public ResponseObject processExchangeList(Long shopId, List<ReturnedStock> returnedStockList, List<ExchangedStock> receivedStockList) {

//        if (null == returnedStockList || returnedStockList.isEmpty()) throw new InventoryAPIOperationException
//                ("Invalid list of returned stock", "Returned stock list is empty or null", null);
//
//        if (null == receivedStockList || receivedStockList.isEmpty()) throw new InventoryAPIOperationException
//                ("Invalid list of exchanged stock", "Exchanged stock list is empty or null", null);
//
//        receivedStockList.stream().map(r -> r.get)
//        if (returnedStockList.size() == receivedStockList.size()){
//            for (int i = 0; i < returnedStockList.size(); i++){
//                 processExchange(shopId, returnedStockList.get(i), receivedStockList.get(i));
//            }
//        }else{
//            List<ReturnedStock> processReturnList = processReturnList(shopId, returnedStockList);
//            if (processReturnList.size() != returnedStockList.size()) throw new InventoryAPIOperationException
//                    ("Cannot proceed", "Exchange could not proceed, review inputs and try again", null);
//
//        }

        return null;
    }

    @Override
    public List<UniqueStock> fetchAllAuthUserUniqueStocks(Long shopId) {

        List<String> stockNames = new ArrayList<>(Collections.emptyList());

        List<ShopStocks> shopStocksList = this.allStockByShopId(shopId);

        stockNames.addAll(
            shopStocksList
                .parallelStream()
                .map(warehouseStocks -> stripStringOfExpiryDate(warehouseStocks.getStockName()))
                .distinct()
                .collect(Collectors.toList())
        );

        return stockNames
            .stream()
            .map(s -> shopStocksList
                .stream()
                .filter(shopStocks -> stripStringOfExpiryDate(shopStocks.getStockName()).equalsIgnoreCase(s))
                .reduce(new ShopStocks(), (s1, s2) -> {

                    if (s1 == null || StringUtils.isEmpty(s1.getStockName())) return s2;
                    String s1Name = stripStringOfExpiryDate(s1.getStockName());
                    if (s1Name.equalsIgnoreCase(stripStringOfExpiryDate(s2.getStockName()))) {
                        s1.setStockQuantityRemaining(s1.getStockQuantityPurchased()
                                + s2.getStockQuantityRemaining());
                    }
                    return s1;
                }))
            .map(ws -> new UniqueStock
                (ws.getShopStockId(), stripStringOfExpiryDate(ws.getStockName()),
                        ws.getStockQuantityRemaining(), ws.getPricePerStockPurchased(), ws.getSellingPricePerStock()))
            .collect(Collectors.toList());
    }

    @Override
    public ShopStocks forceChangeStockQuantity(Long stockId, int newQuantity) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) throw new
                InventoryAPIOperationException("Not allowed", "Operation not allowed for logged in user.", null);

        return shopStocksRepository.findById(stockId).map(shopStock -> {

            if (!shopStock.getShop().getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Shop Stock not yours",
                        "You are attempting to modify a shop stock that is not in your warehouse", null);

            shopStock.setStockQuantitySold(0);
            shopStock.setStockQuantityRemaining(newQuantity);
            shopStock.setStockQuantityPurchased(newQuantity);
            shopStock.setStockPurchasedTotalPrice(BigDecimal.valueOf(newQuantity)
                    .multiply(shopStock.getPricePerStockPurchased()));
            shopStock.setStockRemainingTotalPrice(BigDecimal.valueOf(newQuantity)
                    .multiply(shopStock.getSellingPricePerStock()));
            shopStock.setLastRestockQuantity(newQuantity);
            shopStock.setStockSoldTotalPrice(BigDecimal.ZERO);
            shopStock.setUpdateDate(new Date());
            shopStock.setLastRestockBy(AuthenticatedUserDetails.getUserFullName());

            return shopStocksRepository.save(shopStock);
        }).orElseThrow(() -> new InventoryAPIResourceNotFoundException("Shop stock not found",
                "shop stock with id: " + stockId + " was not found", null));
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

    private BulkUploadResponseWrapper bulkUploadToShopStock(List<ShopStocks> stocksList, Shop shop){

        //Remove any duplicate before you persist the data (duplicates search by name)
        List<ShopStocks> stockToAddList;
        List<String> stockNames = stocksList
                .stream()
                .map(ShopStocks::getStockName)
                .distinct()
                .collect(Collectors.toList());

        //Merge quantities for duplicate stock
        stockToAddList = stockNames
                .stream()
                .map(name -> stocksList
                        .stream()
                        .filter(stock -> stock.getStockName().equalsIgnoreCase(name))
                        .reduce(new ShopStocks(), (t1, t2) -> {

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

        List<ShopStocks> rejectedStockList = new ArrayList<>(Collections.emptyList());
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
                .map(ShopStocks::getStockCategory)
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
                .map(ShopStocks::getLastRestockPurchasedFrom)
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

        List<ShopStocks> stockToSaveList = stockToAddList
                .stream()
                .map(stockToAdd -> {

                    //If there is bar code, do this
                    if (!StringUtils.isEmpty(stockToAdd.getStockBarCodeId())) {

                        ShopStocks stockByBarcode = shopStocksRepository
                                .findDistinctByStockBarCodeId(stockToAdd.getStockBarCodeId());
                        if (stockByBarcode != null
                                && !stockByBarcode.getStockName().equalsIgnoreCase(stockToAdd.getStockName())) {

                            throw new InventoryAPIDuplicateEntryException("Barcode already exist",
                                    "Another stock exist with the barcode id you passed for the new stock you are about to add", null);
                        }
                    }

                    //If stock has quantity from zero and below
                    if (stockToAdd.getStockQuantityPurchased() <= 0){

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

                    ShopStocks existingStock = shopStocksRepository
                            .findDistinctByStockNameAndShop(stockToAdd.getStockName(), shop);

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
                        stockToAdd.setShop(shop);
                        stockToAdd.setLastRestockQuantity(stockToAdd.getStockQuantityPurchased());
                        stockToAdd.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
                        stockToAdd.setLastRestockBy(AuthenticatedUserDetails.getUserFullName());
                        stockToAdd.setStockQuantityRemaining(stockToAdd.getStockQuantityPurchased());
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

        return bulkUploadResponse(shopStocksRepository.saveAll(stockToSaveList), rejectedStockList);
    }

    /*
    This function reverts all income, expenses, debts, discounts and loyalties
    given to an invoice during a sale. This function is only called when a sale is reversed.
    * @return Boolean (True if successful and false otherwise).
    */
    private boolean revertCashFlowOnSaleReversal(String invoiceNumber){

        //Fetch the income, expense, and discounts generated by that invoice number
        List<Income> incomeList = incomeServices.fetchAllByDescriptionContains(invoiceNumber);
        List<Expense> expenseList = expenseServices.fetchExpensesByDescription(invoiceNumber);
        List<SalesDiscount> salesDiscountList = salesDiscountServices.fetchAllSalesDiscountByInvoice(invoiceNumber);

        if (!incomeList.isEmpty())
            incomeList.forEach(income -> incomeServices.deleteEntity(income.getIncomeId()));
        if (!expenseList.isEmpty())
            expenseList.forEach(expense -> expenseServices.deleteEntity(expense.getExpenseId()));
        if (!salesDiscountList.isEmpty())
            salesDiscountList.forEach(salesDiscountServices::deleteSalesDiscount);

        return true;
    }
}
