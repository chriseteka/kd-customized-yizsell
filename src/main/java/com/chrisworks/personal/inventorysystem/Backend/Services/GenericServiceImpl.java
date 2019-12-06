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

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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

    private ShopRepository shopRepository;

    private WarehouseRepository warehouseRepository;

    private StockCategoryRepository stockCategoryRepository;

    @Autowired
    public GenericServiceImpl
            (SupplierRepository supplierRepository, CustomerRepository customerRepository, StockRepository stockRepository,
             InvoiceRepository invoiceRepository, ReturnedStockRepository returnedStockRepository, StockSoldRepository stockSoldRepository,
             ExpenseRepository expenseRepository, IncomeRepository incomeRepository, SellerRepository sellerRepository,
             ShopRepository shopRepository, WarehouseRepository warehouseRepository, BusinessOwnerRepository businessOwnerRepository,
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
        this.shopRepository = shopRepository;
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

        stockSupplier = supplierRepository
                .findDistinctBySupplierPhoneNumber(stockSupplier.getSupplierPhoneNumber());

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

        stockSupplier = supplierRepository.findDistinctBySupplierPhoneNumber(stockSupplier.getSupplierPhoneNumber());

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
        if (totalToAmountPaidDiff.compareTo(BigDecimal.ZERO) >= 0) invoice.setDebt(totalToAmountPaidDiff.abs());

        AtomicReference<Stock> atomicStock = new AtomicReference<>();
        //Decrement the quantity of stock left for all stock in the customers stock bought list
        invoice.getStockSold().forEach(stockSold -> {

            for (Warehouse warehouse : warehouseList) {

                atomicStock.set(stockRepository
                        .findDistinctByStockNameAndWarehouses(stockSold.getStockName(), warehouse));

                if (atomicStock.get() == null) return;

                Stock stockFound = atomicStock.get();

                //Assign a number to the invoice
                invoice.setInvoiceNumber(UniqueIdentifier.invoiceUID());

                //Save all stock bought by the customer as new and independent objects of stockSold, then add them to a set
                stockSold.setCostPricePerStock(stockFound.getSellingPricePerStock());
                stockSold.setStockSoldInvoiceId(invoice.getInvoiceNumber());
                stockSoldSet.add(stockSoldRepository.save(stockSold));

                //Decrementing of overall stock in the store starts now, while profit, and stock sold total price increases
                stockFound.setStockQuantitySold(stockFound.getStockQuantitySold() + stockSold.getQuantitySold());
                stockFound.setStockSoldTotalPrice(stockFound.getProfit().add(stockSold.getCostPricePerStock()
                        .multiply(BigDecimal.valueOf(stockSold.getQuantitySold()))));
                stockFound.setStockQuantityRemaining(stockFound.getStockQuantityRemaining() - stockSold.getQuantitySold());
                stockFound.setProfit(stockFound.getStockSoldTotalPrice().subtract(stockFound.getStockPurchasedTotalPrice()));
                stockRepository.save(stockFound);
            }

            if (atomicStock.get() == null) throw new InventoryAPIResourceNotFoundException
                    ("Stock not found", "Stock with name " + stockSold.getStockName() + ", was not found in any of your warehouses", null);
        });

        //Add amount paid by customer as a new income, this would be needed when balancing inflow and outflow of cash
        String incomeDescription = "Income generated from sale of stock with invoice number: " + invoice.getInvoiceNumber();
        addIncome(new Income(invoice.getAmountPaid(), 100, incomeDescription));

        invoice.getStockSold().clear();
        invoice.setStockSold(stockSoldSet);
        invoice.setCustomerId(customer.get());

        return invoiceRepository.save(invoice);
    }

    @Transactional
    @Override
    public ReturnedStock processReturn(ReturnedStock returnedStock) {

        if (returnedStock == null) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find returned stock entity to save", null);

        AtomicReference<ReturnedStock> returnStock = new AtomicReference<>();

        AtomicReference<StockSold> initialStockSold = new AtomicReference<>();

        AtomicReference<StockSold> updatedStockSold = new AtomicReference<>();

        //Ready warehouses to return stock to
        List<Warehouse> warehouseList = warehouseList();

        Invoice invoiceRetrieved = invoiceRepository.findDistinctByInvoiceNumber(returnedStock.getInvoiceId());

        if(null != invoiceRetrieved){

            invoiceRetrieved.getStockSold().forEach(stockSold -> {

                if (stockSold.getStockName().equalsIgnoreCase(returnedStock.getStockName())) {

                    //Get the initial stock sold object
                    initialStockSold.set(stockSold);

                    //Stock sold is greater than stock returned
                    if (stockSold.getQuantitySold() < returnedStock.getQuantityReturned()) throw new InventoryAPIOperationException
                            ("Quantity returned is invalid", "Quantity returned is above quantity sold, review your inputs", null);

                    Stock stockToReturn = null;

                    for (Warehouse warehouse : warehouseList) {

                        stockToReturn = stockRepository
                                .findDistinctByStockNameAndWarehouses(returnedStock.getStockName(), warehouse);

                        if (null == stockToReturn) return;

                        //Save returns
                        returnedStock.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
                        returnedStock.setCustomerId(invoiceRetrieved.getCustomerId());
                        returnedStock.setStockReturnedCost(BigDecimal.valueOf(returnedStock.getQuantityReturned())
                                .multiply(stockToReturn.getSellingPricePerStock()));
                        returnStock.set(returnedStockRepository.save(returnedStock));

                        //Update stock left after return
                        stockToReturn.setStockRemainingTotalPrice(stockToReturn.getStockRemainingTotalPrice()
                                .add(BigDecimal.valueOf(returnedStock.getQuantityReturned())
                                        .multiply(stockSold.getCostPricePerStock())));
                        stockToReturn.setStockQuantityRemaining(returnedStock.getQuantityReturned() +
                                stockToReturn.getStockQuantityRemaining());
                        stockToReturn.setProfit(stockToReturn.getProfit()
                                .subtract(BigDecimal.valueOf(returnedStock.getQuantityReturned())
                                        .multiply(stockToReturn.getSellingPricePerStock())));
                        stockToReturn.setStockSoldTotalPrice(stockToReturn.getStockSoldTotalPrice()
                                .subtract(BigDecimal.valueOf(returnedStock.getQuantityReturned())
                                        .multiply(stockSold.getPricePerStockSold())));
                        stockToReturn.setStockQuantitySold(stockToReturn.getStockQuantitySold() -
                                returnedStock.getQuantityReturned());
                        stockToReturn.setUpdateDate(new Date());

                        stockRepository.save(stockToReturn);

                        //Update stockSold
                        //Reduce quantity of stock from the stock sold table, after find
                        StockSold initStockSold = stockSoldRepository.findDistinctByStockSoldInvoiceIdAndStockName
                                (returnedStock.getInvoiceId(), stockSold.getStockName());
                        initStockSold.setUpdateDate(new Date());
                        initStockSold.setQuantitySold(initStockSold.getQuantitySold() - returnedStock.getQuantityReturned());
                        updatedStockSold.set(stockSoldRepository.save(initStockSold));
                    }

                    //Could not find the stock to return any of the warehouses
                    if(stockToReturn == null) throw new InventoryAPIResourceNotFoundException
                            ("Stock not found", "The stock about to be returned was never existed in any of your warehouses", null);
                }
            });

            //Update Invoice
            Set<StockSold> stockSoldSet = invoiceRetrieved.getStockSold();
            stockSoldSet.remove(initialStockSold.get());
            stockSoldSet.add(updatedStockSold.get());
            invoiceRetrieved.setStockSold(stockSoldSet);
            invoiceRepository.save(invoiceRetrieved);

            //Create an Expense of type sales_return and save it
            String expenseDescription = returnStock.get().getStockName() + " returned with reason: " + returnStock.get().getReasonForReturn();
            Expense expenseOnReturn = addExpense(new Expense(300, returnStock.get().getStockReturnedCost(), expenseDescription));

            //Do the following if user is a seller
            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SELLER)) {

                //Add the returned stock to the set of returned stock in the shop it was returned
                Shop stockReturnedShop = shopBySellerName(AuthenticatedUserDetails.getUserFullName());
                Set<ReturnedStock> allReturnedSales = stockReturnedShop.getReturnedSales();
                allReturnedSales.add(returnStock.get());
                stockReturnedShop.setReturnedSales(allReturnedSales);

                //Add the newly created expense to the set of expenses incurred in the shop
                Set<Expense> allExpensesInShop = stockReturnedShop.getExpenses();
                allExpensesInShop.add(expenseOnReturn);
                stockReturnedShop.setExpenses(allExpensesInShop);

                shopRepository.save(stockReturnedShop);
            }

        }else throw new InventoryAPIResourceNotFoundException
                ("Invoice not found", "Invoice not found for the returned stock invoice number", null);

        return returnStock.get();
    }

    @Override
    public List<ReturnedStock> processReturnList(List<ReturnedStock> returnedStockList) {

        if (null == returnedStockList || returnedStockList.isEmpty()) throw new InventoryAPIOperationException
                ("Invalid list of returned stock", "Returned stock list is empty or null", null);

        return returnedStockList.stream().map(this::processReturn).collect(Collectors.toList());
    }

    @Override
    public Expense addExpense(Expense expense) {

        if (null == expense) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find expense entity to save", null);

        expense.setCreatedBy(AuthenticatedUserDetails.getUserFullName());

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            expense.setApproved(true);
            expense.setApprovedDate(new Date());
            expense.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
        }

        return expenseRepository.save(expense);
    }

    @Override
    public Income addIncome(Income income) {

        if (null == income) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find income entity to save", null);

        income.setCreatedBy(AuthenticatedUserDetails.getUserFullName());

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            income.setApproved(true);
            income.setApprovedDate(new Date());
            income.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
        }

        return incomeRepository.save(income);
    }

    @Override
    public Stock changeStockSellingPriceById(Long stockId, BigDecimal newSellingPrice) {

        if (null == stockId || stockId < 0 || !stockId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("stock id error", "stock id is empty or not a valid number", null);

        if (null == newSellingPrice || newSellingPrice.compareTo(BigDecimal.ZERO) <= 0 || !newSellingPrice.toString().matches("\\d+"))
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

        if (null == newSellingPrice || newSellingPrice.compareTo(BigDecimal.ZERO) <= 0 || !newSellingPrice.toString().matches("\\d+"))
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

        Seller sellerFound = sellerRepository.findDistinctBySellerFullName(sellerName);

        if (null == sellerFound) throw new InventoryAPIResourceNotFoundException
                ("Seller not retrieved", "No shop exist with a seller named: " + sellerName, null);

        return shopRepository.findDistinctBySellers(sellerFound);
    }

    private Stock changeStockSellingPrice(Stock stock, BigDecimal newSellingPrice) {

        stock.setUpdateDate(new Date());
        stock.setSellingPricePerStock(newSellingPrice);

        //If stock is added by user with the role as admin, then tag the stock as approved first, approvedDate
        // and approvedBy to admin's name. Else set approved to false.

        return stock;
    }

    private Warehouse fetchAuthUserWarehouse(Long warehouseId){

        Optional<Warehouse> optionalWarehouse = warehouseRepository.findById(warehouseId);

        if (!optionalWarehouse.isPresent()) throw new InventoryAPIResourceNotFoundException
                ("warehouse not found", "warehouse not found with the id: " + warehouseId, null);

        return optionalWarehouse.get();

    }

    private List<Warehouse> warehouseList(){

        List<Warehouse> warehouseList = new ArrayList<>();

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

            Optional<BusinessOwner> businessOwner = businessOwnerRepository.findById(AuthenticatedUserDetails.getUserId());

            businessOwner.ifPresent(owner -> warehouseList.addAll(warehouseRepository.findAllByBusinessOwner(owner)));
        }
        else{

            Optional<Seller> optionalSeller = sellerRepository.findById(AuthenticatedUserDetails.getUserId());

            optionalSeller.ifPresent(seller -> warehouseList.addAll(shopRepository.findDistinctBySellers(seller).getWarehouses()));
        }

        if (warehouseList.isEmpty()) throw new InventoryAPIResourceNotFoundException
                ("warehouse list not found", "no warehouse was found with active user details", null);

        return warehouseList;
    }
}
