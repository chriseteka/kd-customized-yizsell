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

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;
import static ir.cafebabe.math.utils.BigDecimalUtils.is;

/**
 * @author Chris_Eteka
 * @since 11/27/2019
 * @email chriseteka@gmail.com
 */
@Service
public class GenericServiceImpl implements GenericService {

    private  BusinessOwnerRepository businessOwnerRepository;

    private SupplierRepository supplierRepository;

    private CustomerRepository customerRepository;

    private StockRepository stockRepository;

    private InvoiceRepository invoiceRepository;

    private ReturnedStockRepository returnedStockRepository;

    private StockSoldRepository stockSoldRepository;

    private ExpenseRepository expenseRepository;

    private IncomeRepository incomeRepository;

    private SellerRepository sellerRepository;

    private WarehouseRepository warehouseRepository;

    private StockCategoryRepository stockCategoryRepository;

    @Autowired
    public GenericServiceImpl
            (SupplierRepository supplierRepository, CustomerRepository customerRepository, StockRepository stockRepository,
             InvoiceRepository invoiceRepository, ReturnedStockRepository returnedStockRepository, StockSoldRepository stockSoldRepository,
             ExpenseRepository expenseRepository, IncomeRepository incomeRepository, SellerRepository sellerRepository,
             WarehouseRepository warehouseRepository, BusinessOwnerRepository businessOwnerRepository,
             StockCategoryRepository stockCategoryRepository)
    {
        this.supplierRepository = supplierRepository;
        this.customerRepository = customerRepository;
        this.stockRepository = stockRepository;
        this.invoiceRepository = invoiceRepository;
        this.returnedStockRepository = returnedStockRepository;
        this.stockSoldRepository = stockSoldRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.sellerRepository = sellerRepository;
        this.warehouseRepository = warehouseRepository;
        this.businessOwnerRepository = businessOwnerRepository;
        this.stockCategoryRepository = stockCategoryRepository;
    }

    @Override
    public Customer addCustomer(Customer customer) {

        if (null == customer) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find customer entity to save", null);

        customer.setCreatedBy(AuthenticatedUserDetails.getUserFullName());

        return customerRepository.save(customer);
    }

    @Override
    public Supplier addSupplier(Supplier supplier) {

        if (null == supplier) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find supplier entity to save", null);

        if (supplierRepository.findBySupplierPhoneNumber(supplier.getSupplierPhoneNumber()) != null) throw new
                InventoryAPIDuplicateEntryException("Duplicate entry", "Supplier with same phone number exists", null);

        supplier.setCreatedBy(AuthenticatedUserDetails.getUserFullName());

        return supplierRepository.save(supplier);
    }

    @Override
    public StockCategory addStockCategory(StockCategory stockCategory) {

        if (null == stockCategory) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find stockCategory entity to save", null);

        stockCategory.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
        return stockCategoryRepository.save(stockCategory);
    }

    @Transactional
    @Override
    public Stock addStock(Long warehouseId, Stock stock) {

        if (null == warehouseId || warehouseId < 0 || !warehouseId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("warehouse id error", "warehouse id is empty or not a valid number", null);

        if (null == stock) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find stock entity to save", null);

        Supplier stockSupplier = stock.getLastRestockPurchasedFrom();

        StockCategory stockCategory = stock.getStockCategory();

        stockCategory = stockCategoryRepository.findDistinctFirstByCategoryName(stockCategory.getCategoryName());

        if (null == stockCategory) stockCategory = addStockCategory(stock.getStockCategory());

        System.out.println(stockSupplier);
        stockSupplier = supplierRepository
                .findBySupplierPhoneNumber(stockSupplier.getSupplierPhoneNumber());

        if (null == stockSupplier) stockSupplier = addSupplier(stock.getLastRestockPurchasedFrom());

        Warehouse warehouse = fetchAuthUserWarehouse(warehouseId);
//        if (optionalWarehouse.get().getBusinessOwner() != bussOwnerAddingIt) throw error and return

        Stock existingStock = stockRepository
                .findDistinctByStockNameAndWarehouses(stock.getStockName(), warehouse);

        if (existingStock != null){

            existingStock.setLastRestockPurchasedFrom(stockSupplier);

            return reStock(warehouseId, existingStock.getStockId(), stock);
        }

        Set<Supplier> supplierSet = new HashSet<>();
        supplierSet.add(stockSupplier);

        Set<Warehouse> warehouseSet = new HashSet<>();
        warehouseSet.add(warehouse);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            stock.setApproved(true);
            stock.setApprovedDate(new Date());
            stock.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
        }

        stock.setStockCategory(stockCategory);
        stock.setStockPurchasedFrom(supplierSet);
        stock.setWarehouses(warehouseSet);
        stock.setLastRestockPurchasedFrom(stockSupplier);
        stock.setLastRestockQuantity(stock.getStockQuantityPurchased());
        stock.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
        stock.setLastRestockBy(AuthenticatedUserDetails.getUserFullName());
        stock.setStockQuantityRemaining(stock.getStockQuantityPurchased());
        stock.setStockRemainingTotalPrice(stock.getStockPurchasedTotalPrice());
        stock.setPricePerStockPurchased(stock.getStockPurchasedTotalPrice()
                .divide(BigDecimal.valueOf(stock.getStockQuantityPurchased()), 2));

        return stockRepository.save(stock);
    }

    @Transactional
    @Override
    public Stock reStock(Long warehouseId, Long stockId, Stock newStock) {

        if (null == warehouseId || warehouseId < 0 || !warehouseId.toString().matches("\\d+")
            || null == stockId || stockId < 0 || !stockId.toString().matches("\\d+")) throw new InventoryAPIOperationException
                ("warehouse id or stock id error", "warehouse id and/or stock id is empty or not a valid number", null);

        if (null == newStock) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find stock entity to save", null);

        Warehouse warehouse = fetchAuthUserWarehouse(warehouseId);
//        if (optionalWarehouse.get().getBusinessOwner() != bussOwnerAddingIt) throw error and return

        Supplier stockSupplier = newStock.getLastRestockPurchasedFrom();

        stockSupplier = supplierRepository.findBySupplierPhoneNumber(stockSupplier.getSupplierPhoneNumber());

        if (null == stockSupplier) stockSupplier = addSupplier(newStock.getLastRestockPurchasedFrom());

        AtomicReference<Stock> reStock = new AtomicReference<>();

        Supplier finalStockSupplier = stockSupplier;

        Optional<Stock> optionalStock = stockRepository.findById(stockId);

        if (!optionalStock.isPresent()) throw new InventoryAPIOperationException
                ("could not find an entity", "Could not find stock with the id " + stockId, null);

        optionalStock.ifPresent(stock -> {

            Set<Warehouse> allWarehouses = stock.getWarehouses();
            Set<Supplier> allSuppliers = stock.getStockPurchasedFrom();
            allSuppliers.add(finalStockSupplier);
            allWarehouses.add(warehouse);
            stock.setUpdateDate(new Date());
            stock.setStockPurchasedFrom(allSuppliers);
            stock.setWarehouses(allWarehouses);
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
            }else {

                stock.setApproved(false);
                stock.setApprovedBy(null);
                stock.setApprovedBy(null);
            }

            reStock.set(stockRepository.save(stock));

        });

        return reStock.get();
    }

    @Transactional
    @Override
    public Invoice sellStock(Invoice invoice) {

        if (null == invoice) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find valid data to generate an invoice", null);

        AtomicReference<Customer> customer = new AtomicReference<>();

        Set<StockSold> stockSoldSet = new HashSet<>();

        //Ready warehouses to fetch stock from
        List<Warehouse> warehouseList = warehouseList();

        //Find a customer by phone number
        customer.set(
                customerRepository.findDistinctByCustomerPhoneNumber(invoice.getCustomerId().getCustomerPhoneNumber()));

        //If customer does not exist before, save it.
        if (null == customer.get()) customer.set(this.addCustomer(invoice.getCustomerId()));

        //Get a difference between total amount in invoice and the amount paid by the customer, noting any discount
        BigDecimal totalToAmountPaidDiff = invoice.getInvoiceTotalAmount()
                .subtract(invoice.getAmountPaid().add(invoice.getDiscount()));

        //If customer under pays for list of stocks in his invoice, record a debt against the invoice.
        if (is(totalToAmountPaidDiff).isPositive()) invoice.setDebt(totalToAmountPaidDiff.abs());

        AtomicReference<Stock> atomicStock = new AtomicReference<>();

        //Assign a number to the invoice
        invoice.setInvoiceNumber(UniqueIdentifier.invoiceUID());

        //Decrement the quantity of stock left for all stock in the customers stock bought list
        invoice.getStockSold().forEach(stockSold -> {

            for (Warehouse warehouse : warehouseList) {

                atomicStock.set(stockRepository
                        .findDistinctByStockNameAndWarehouses(stockSold.getStockName(), warehouse));

                if (atomicStock.get() == null) return;

                Stock stockFound = atomicStock.get();

                if (stockFound.getStockQuantityRemaining() < stockSold.getQuantitySold()) throw new InventoryAPIOperationException
                        ("Low stock quantity", "The quantity of " + stockFound.getStockName() +
                                " is limited, and you cannot sell above it.", null);

                //Save all stock bought by the customer as new and independent objects of stockSold, then add them to a set
                stockSold.setCostPricePerStock(stockFound.getPricePerStockPurchased());
                stockSold.setStockSoldInvoiceId(invoice.getInvoiceNumber());
                stockSoldSet.add(stockSoldRepository.save(stockSold));

                //Decrementing of overall stock in the store starts now, while profit, and stock sold total price increases
                stockFound.setStockQuantitySold(stockFound.getStockQuantitySold() + stockSold.getQuantitySold());
                stockFound.setStockSoldTotalPrice(stockFound.getStockSoldTotalPrice().add(stockSold.getCostPricePerStock()
                        .multiply(BigDecimal.valueOf(stockSold.getQuantitySold()))));
                stockFound.setStockQuantityRemaining(stockFound.getStockQuantityRemaining() - stockSold.getQuantitySold());
                stockFound.setStockRemainingTotalPrice(BigDecimal.valueOf(stockFound.getStockQuantityRemaining())
                        .multiply(stockFound.getPricePerStockPurchased()));
                stockFound.setProfit(stockFound.getStockSoldTotalPrice().subtract(stockFound.getStockPurchasedTotalPrice()));
                stockRepository.save(stockFound);
            }

            if (atomicStock.get() == null) throw new InventoryAPIResourceNotFoundException
                    ("Stock not found", "Stock with name " + stockSold.getStockName() + ", was not found in any of your warehouses", null);
        });

        //Add amount paid by customer as a new income, this would be needed when balancing inflow and outflow of cash
        String incomeDescription = "Income generated from sale of stock with invoice number: " + invoice.getInvoiceNumber();
        addIncome(new Income(invoice.getAmountPaid(), 100, incomeDescription));

        String invoiceGeneratedBy = AuthenticatedUserDetails.getUserFullName();

        invoice.getStockSold().clear();
        invoice.setStockSold(stockSoldSet);
        invoice.setCustomerId(customer.get());
        invoice.setCreatedBy(invoiceGeneratedBy);

        if (ACCOUNT_TYPE.SELLER.equals(AuthenticatedUserDetails.getAccount_type()))
            invoice.setSeller(sellerRepository.findDistinctBySellerFullNameOrSellerEmail(invoiceGeneratedBy, invoiceGeneratedBy));

        return invoiceRepository.save(invoice);
    }

    @Transactional
    @Override
    public ReturnedStock processReturn(ReturnedStock returnedStock) {

        if (returnedStock == null) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find returned stock entity to save", null);

        ReturnedStock returnStock;

        //Search invoice repository for the invoice number a return is about to be made with
        Invoice invoiceRetrieved = invoiceRepository.findDistinctByInvoiceNumber(returnedStock.getInvoiceId());

        if (null == invoiceRetrieved) throw new InventoryAPIResourceNotFoundException
                ("No invoice was found by id", "No invoice with id " + returnedStock.getInvoiceId() + " was found", null);

        //Ready warehouses to return stock to
        List<Warehouse> warehouseList = warehouseList();

        //search warehouses for the stock about to be returned
        Stock stockRecordFromWarehouse = warehouseList.stream()
                .map(warehouse -> stockRepository
                        .findDistinctByStockNameAndWarehouses(returnedStock.getStockName(), warehouse))
                .collect(toSingleton());

        if (null == stockRecordFromWarehouse) throw new InventoryAPIResourceNotFoundException
                ("Stock not found", "The stock about to be returned never existed in any of your warehouses", null);

            //Search the invoice retrieved for the stock about to be returned
            StockSold stockAboutToBeReturned = invoiceRetrieved.getStockSold()
                    .stream()
                    .filter(stockSold -> stockSold.getStockName().equalsIgnoreCase(returnedStock.getStockName()))
                    .collect(toSingleton());

            if (null == stockAboutToBeReturned) throw new InventoryAPIOperationException("Stock doe not exist in the invoice",
                    "The stock about to be returned does not exist in the list of stocks sold with the invoice", null);

            //Stock sold is greater than stock returned
            if (stockAboutToBeReturned.getQuantitySold() < returnedStock.getQuantityReturned()) throw new InventoryAPIOperationException
                    ("Quantity returned is invalid", "Quantity returned is above quantity sold, review your inputs", null);

            //Save returns
            returnedStock.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
            returnedStock.setCustomerId(invoiceRetrieved.getCustomerId());
            returnedStock.setStockReturnedCost(BigDecimal.valueOf(returnedStock.getQuantityReturned())
                    .multiply(stockRecordFromWarehouse.getSellingPricePerStock()));

            //Update stock left after return
            stockRecordFromWarehouse.setStockRemainingTotalPrice(stockRecordFromWarehouse.getStockRemainingTotalPrice()
                    .add(BigDecimal.valueOf(returnedStock.getQuantityReturned())
                            .multiply(stockAboutToBeReturned.getCostPricePerStock())));
            stockRecordFromWarehouse.setStockQuantityRemaining(returnedStock.getQuantityReturned() +
                    stockRecordFromWarehouse.getStockQuantityRemaining());
            stockRecordFromWarehouse.setProfit(stockRecordFromWarehouse.getProfit()
                    .subtract(BigDecimal.valueOf(returnedStock.getQuantityReturned())
                            .multiply(stockRecordFromWarehouse.getSellingPricePerStock())));
            stockRecordFromWarehouse.setStockSoldTotalPrice(stockRecordFromWarehouse.getStockSoldTotalPrice()
                    .subtract(BigDecimal.valueOf(returnedStock.getQuantityReturned())
                            .multiply(stockAboutToBeReturned.getPricePerStockSold())));
            stockRecordFromWarehouse.setStockQuantitySold(stockRecordFromWarehouse.getStockQuantitySold() -
                    returnedStock.getQuantityReturned());
            stockRecordFromWarehouse.setUpdateDate(new Date());

            stockRepository.save(stockRecordFromWarehouse);

            //Update stockSold
            //Reduce quantity of stock from the stock sold table, after find
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
                    invoiceRetrieved.setStockSold(stockSoldSet);
                    invoiceRetrieved.setPaymentModeVal(String.valueOf(invoiceRetrieved.getPaymentModeValue()));
                    invoiceRepository.save(invoiceRetrieved);
                }
            }

            //Create an Expense of type sales_return and save it
            String expenseDescription = returnedStock.getStockName() + " returned with reason: " + returnedStock.getReasonForReturn();
            Expense expenseOnReturn = new Expense(300, returnedStock.getStockReturnedCost(), expenseDescription);

            //Do the following if user is a seller
            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SELLER)) {

                //Add the returned stock and new expense created to the shop of that seller
                Shop stockReturnedShop = shopBySellerName(AuthenticatedUserDetails.getUserFullName());
                returnedStock.setShop(stockReturnedShop);

                addExpense(expenseOnReturn);
                returnStock = returnedStockRepository.save(returnedStock);
            }else{

                returnedStock.setApproved(true);
                returnedStock.setApprovedDate(new Date());
                returnedStock.setApprovedBy(AuthenticatedUserDetails.getUserFullName());

                addExpense(expenseOnReturn);
                returnStock = returnedStockRepository.save(returnedStock);
            }

        return returnStock;
    }

    @Override
    public List<ReturnedStock> processReturnList(List<ReturnedStock> returnedStockList) {

        if (null == returnedStockList || returnedStockList.isEmpty()) throw new InventoryAPIOperationException
                ("Invalid list of returned stock", "Returned stock list is empty or null", null);

        return returnedStockList.stream().map(this::processReturn).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Expense addExpense(Expense expense) {

        if (null == expense) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find expense entity to save", null);

        expense.setCreatedBy(AuthenticatedUserDetails.getUserFullName());

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            expense.setApproved(true);
            expense.setApprovedDate(new Date());
            expense.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
            return expenseRepository.save(expense);
        }else{

            //get the seller's shop, add the expense to it then persist it
            expense.setShop(shopBySellerName(AuthenticatedUserDetails.getUserFullName()));

            return expenseRepository.save(expense);
        }
    }

    @Override
    @Transactional
    public Income addIncome(Income income) {

        if (null == income) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find income entity to save", null);

        income.setCreatedBy(AuthenticatedUserDetails.getUserFullName());

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            income.setApproved(true);
            income.setApprovedDate(new Date());
            income.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
            return incomeRepository.save(income);
        }else{

            //get the seller's shop, add the income to it then persist it
            Shop distinctShopBySeller = shopBySellerName(AuthenticatedUserDetails.getUserFullName());
            income.setShop(distinctShopBySeller);

            return incomeRepository.save(income);
        }
    }

    @Override
    public Stock changeStockSellingPriceById(Long stockId, BigDecimal newSellingPrice) {

        if (null == stockId || stockId < 0 || !stockId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("stock id error", "stock id is empty or not a valid number", null);

        if (null == newSellingPrice || is(newSellingPrice).lte(BigDecimal.ZERO) || !newSellingPrice.toString().matches("\\d+"))
            throw new InventoryAPIOperationException("selling price error", "selling price is empty or not a valid number", null);

        AtomicReference<Stock> updatedStock = new AtomicReference<>();

        stockRepository.findById(stockId).ifPresent(stock ->

                updatedStock.set(stockRepository.save(changeStockSellingPrice(stock, newSellingPrice))));

        return updatedStock.get();
    }

    @Override
    public Stock changeStockSellingPriceByWarehouseIdAndStockName
            (Long warehouseId, String stockName, BigDecimal newSellingPrice) {

        if (null == warehouseId || warehouseId < 0 || !warehouseId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("warehouse id error", "warehouse id is empty or not a valid number", null);

        if (null == stockName || stockName.isEmpty()) throw new
                InventoryAPIOperationException("stock name error", "stock name is empty or null", null);

        if (null == newSellingPrice || is(newSellingPrice).lte(BigDecimal.ZERO) || !newSellingPrice.toString().matches("\\d+"))
            throw new InventoryAPIOperationException("selling price error", "selling price is empty or not a valid number", null);

        Warehouse warehouse = fetchAuthUserWarehouse(warehouseId);

        Stock stockRetrieved = stockRepository.findDistinctByStockNameAndWarehouses(stockName, warehouse);

        if (null != stockRetrieved) return stockRepository.save(changeStockSellingPrice(stockRetrieved, newSellingPrice));

        return null;
    }

    @Override
    public Shop shopBySellerName(String sellerName) {

        if (null == sellerName || sellerName.isEmpty()) throw new
                InventoryAPIOperationException("seller name error", "seller name is empty or null", null);

        Seller sellerFound = sellerRepository.findDistinctBySellerFullNameOrSellerEmail(sellerName, sellerName);

        if (null == sellerFound) throw new InventoryAPIResourceNotFoundException
                ("Seller not retrieved", "Seller with name: " + sellerName + " was not found.", null);

        return sellerFound.getShop();
    }

    @Override
    public List<Warehouse> allWarehouseByAuthUserId() {

        Long authUserId = AuthenticatedUserDetails.getUserId();

        ACCOUNT_TYPE authUserType = AuthenticatedUserDetails.getAccount_type();

        String authUserMail = AuthenticatedUserDetails.getUserFullName();

        if (null == authUserId || authUserId < 0 || !authUserId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("authUserId id error", "Authenticated user id is empty or not a valid number", null);

        if (authUserType.equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            return businessOwnerRepository.findById(authUserId)
                    .map(warehouseRepository::findAllByBusinessOwner)
                    .orElse(Collections.emptyList());
        if (authUserType.equals(ACCOUNT_TYPE.SELLER)){

            Seller sellerFound = sellerRepository.findDistinctBySellerFullNameOrSellerEmail(authUserMail, authUserMail);
            return new ArrayList<>(Collections.singleton(sellerFound.getShop().getWarehouse()));
        }

        return null;
    }

    @Override
    public List<Supplier> fetchSuppliersByCreator(String createdBy) {

        return supplierRepository.findAllByCreatedBy(createdBy);
    }

    @Override
    public List<StockCategory> fetchAllStockCategoryByCreator(String businessOwnerMail) {

        return stockCategoryRepository.findAllByCreatedBy(businessOwnerMail);
    }

    private Stock changeStockSellingPrice(Stock stock, BigDecimal newSellingPrice) {

        stock.setUpdateDate(new Date());
        stock.setSellingPricePerStock(newSellingPrice);

        //If stock is added by user with the role as admin, then tag the stock as approved first, approvedDate
        // and approvedBy to admin's name. Else set approved to false.

        return stock;
    }

    private Warehouse fetchAuthUserWarehouse(Long warehouseId){

        if (!warehouseRepository.findById(warehouseId).isPresent()) throw new InventoryAPIOperationException
                ("warehouse id error", "No warehouse was retrieved with the id: " + warehouseId, null);

        return allWarehouseByAuthUserId()
                .stream()
                .filter(warehouse -> warehouse.getWarehouseId().equals(warehouseId))
                .collect(toSingleton());

    }

    private List<Warehouse> warehouseList(){

        List<Warehouse> warehouseList = new ArrayList<>();

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

            Optional<BusinessOwner> businessOwner = businessOwnerRepository.findById(AuthenticatedUserDetails.getUserId());

            businessOwner.ifPresent(owner -> warehouseList.addAll(warehouseRepository.findAllByBusinessOwner(owner)));
        }
        else{

            Optional<Seller> optionalSeller = sellerRepository.findById(AuthenticatedUserDetails.getUserId());

            optionalSeller.ifPresent(seller -> warehouseList.add(seller.getShop().getWarehouse()));
        }

        if (warehouseList.isEmpty()) throw new InventoryAPIResourceNotFoundException
                ("warehouse list not found", "no warehouse was found with active user details", null);

        return warehouseList;
    }
}
