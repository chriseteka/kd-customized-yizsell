package com.chrisworks.personal.inventorysystem.Backend.Services.GenericServices.Implementation;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.*;
import com.chrisworks.personal.inventorysystem.Backend.Services.GenericServices.GenericService;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.chrisworks.personal.inventorysystem.Backend.Utility.UniqueIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Chris_Eteka
 * @since 11/27/2019
 * @email chriseteka@gmail.com
 */
@Service
public class GenericServiceImpl implements GenericService {

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

    @Autowired
    public GenericServiceImpl(SupplierRepository supplierRepository, CustomerRepository customerRepository,
                              StockRepository stockRepository, InvoiceRepository invoiceRepository,
                              ReturnedStockRepository returnedStockRepository, StockSoldRepository stockSoldRepository,
                              ExpenseRepository expenseRepository, IncomeRepository incomeRepository,
                              SellerRepository sellerRepository, ShopRepository shopRepository) {
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

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            stock.setApproved(true);
            stock.setApprovedDate(new Date());
            stock.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
        }

        stock.setStockPurchasedFrom(supplierSet);
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
    public Stock reStock(Long stockId, Stock newStock, Supplier supplier) {

        Supplier stockSupplier;

        stockSupplier = supplierRepository.findDistinctBySupplierPhoneNumber(supplier.getSupplierPhoneNumber());

        if (null == stockSupplier) stockSupplier = addSupplier(supplier);

        AtomicReference<Stock> reStock = new AtomicReference<>();

        Supplier finalStockSupplier = stockSupplier;

        stockRepository.findById(stockId).ifPresent(stock -> {

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

        AtomicReference<Customer> customer = new AtomicReference<>();

        Set<StockSold> stockSoldSet = new HashSet<>();

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

        //Decrement the quantity of stock left for all stock in the customers stock bought list
        invoice.getStockSold().forEach(stockSold -> {

            Stock stockFound = stockRepository.findDistinctByStockName(stockSold.getStockName());
            if (stockFound == null){

                //throw error and return
                return;
            }

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

        AtomicReference<ReturnedStock> returnStock = new AtomicReference<>();

        AtomicReference<StockSold> initialStockSold = new AtomicReference<>();

        AtomicReference<StockSold> updatedStockSold = new AtomicReference<>();

        Invoice invoiceRetrieved = invoiceRepository.findDistinctByInvoiceNumber(returnedStock.getInvoiceId());

        if(null != invoiceRetrieved){

            invoiceRetrieved.getStockSold().forEach(stockSold -> {

                if (stockSold.getStockName().equalsIgnoreCase(returnedStock.getStockName())){

                    //Get the initial stock sold object
                    initialStockSold.set(stockSold);

                    //Stock sold is greater than stock returned
                    if (stockSold.getQuantitySold() < returnedStock.getQuantityReturned()){

                        //Error to be thrown here
                        return;
                    }

                    Stock stockToReturn = stockRepository.findDistinctByStockName(returnedStock.getStockName());

                    if (null == stockToReturn){

                        //Error that stock about to be returned does not exist in the store
                        return;
                    }

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

        }else{

            //Throw error that invoice was not retrieved
        }

        return returnStock.get();
    }

    @Override
    public List<ReturnedStock> processReturnList(List<ReturnedStock> returnedStockList) {

        if (null == returnedStockList || returnedStockList.isEmpty()){

            //Throw error, list empty
            return null;
        }

        return returnedStockList.stream().map(this::processReturn).collect(Collectors.toList());
    }

    @Override
    public Expense addExpense(Expense expense) {

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

        AtomicReference<Stock> updatedStock = new AtomicReference<>();

        stockRepository.findById(stockId).ifPresent(stock ->

                updatedStock.set(stockRepository.save(changeStockSellingPrice(stock, newSellingPrice))));

        return updatedStock.get();
    }

    @Override
    public Stock changeStockSellingPriceByName(String stockName, BigDecimal newSellingPrice) {

        Stock stockRetrieved = stockRepository.findDistinctByStockName(stockName);

        if (null != stockRetrieved) return stockRepository.save(changeStockSellingPrice(stockRetrieved, newSellingPrice));

        return null;
    }

    @Override
    public Shop shopBySellerName(String sellerName) {

        Seller sellerFound = sellerRepository.findDistinctBySellerFullName(sellerName);

        if (null == sellerFound){

            //Throw error
            return null;
        }

        return shopRepository.findDistinctBySellers(sellerFound);
    }

    private Stock changeStockSellingPrice(Stock stock, BigDecimal newSellingPrice) {

        stock.setUpdateDate(new Date());
        stock.setSellingPricePerStock(newSellingPrice);

        //If stock is added by user with the role as admin, then tag the stock as approved first, approvedDate
        // and approvedBy to admin's name. Else set approved to false.

        return stock;
    }
}
