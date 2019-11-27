package com.chrisworks.personal.inventorysystem.Backend.Services.GenericServices.Implementation;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.*;
import com.chrisworks.personal.inventorysystem.Backend.Services.GenericServices.GenericService;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
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

    @Autowired
    public GenericServiceImpl(SupplierRepository supplierRepository, CustomerRepository customerRepository,
                              StockRepository stockRepository, InvoiceRepository invoiceRepository,
                              ReturnedStockRepository returnedStockRepository) {
        this.supplierRepository = supplierRepository;
        this.customerRepository = customerRepository;
        this.stockRepository = stockRepository;
        this.invoiceRepository = invoiceRepository;
        this.returnedStockRepository = returnedStockRepository;
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

        stock.setStockPurchasedFrom(supplierSet);
        stock.setLastRestockPurchasedFrom(stockSupplier);
        stock.setLastRestockQuantity(stock.getStockQuantityPurchased());
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

            //If stock is added by user with the role as admin, then tag the stock as approved first, set createdBy,
            // lastRestockBy and approvedBy to admin's name. Else set createdBy and lastRestockBy as seller name.

            reStock.set(stockRepository.save(stock));

        });

        return reStock.get();
    }

    @Transactional
    @Override
    public Invoice sellStock(Invoice invoice) {
        return null;
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
                    stockSold.setUpdateDate(new Date());
                    stockSold.setQuantitySold(stockSold.getQuantitySold() - returnedStock.getQuantityReturned());
                    updatedStockSold.set(stockSoldRepository.save(stockSold));
                }
            });

            //Update Invoice
            Set<StockSold> stockSoldSet = invoiceRetrieved.getStockSold();
            stockSoldSet.remove(initialStockSold.get());
            stockSoldSet.add(updatedStockSold.get());
            invoiceRetrieved.setStockSold(stockSoldSet);
            invoiceRepository.save(invoiceRetrieved);

        }else{

            //Throw error that invoice was not retrieved
        }

        return returnStock.get();
    }

    @Override
    public ReturnedStock processReturnList(List<ReturnedStock> returnedStockList) {

        AtomicReference<ReturnedStock> returnStock = new AtomicReference<>();

        if (null == returnedStockList || returnedStockList.isEmpty()){

            //Throw error, list empty
            return null;
        }

        returnedStockList.forEach(returnedStock -> returnStock.set(processReturn(returnedStock)));

        return returnStock.get();
    }

    @Override
    public Expense addExpense(Expense expense) {
        return null;
    }

    @Override
    public Income addIncome(Income income) {
        return null;
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

    private Stock changeStockSellingPrice(Stock stock, BigDecimal newSellingPrice) {

        stock.setUpdateDate(new Date());
        stock.setSellingPricePerStock(newSellingPrice);

        //If stock is added by user with the role as admin, then tag the stock as approved first, approvedDate
        // and approvedBy to admin's name. Else set approved to false.

        return stock;
    }
}
