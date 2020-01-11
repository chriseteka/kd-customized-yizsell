package com.chrisworks.personal.inventorysystem.Backend.Utility;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.getDateDifferenceInDays;

/**
 * @author Chris_Eteka
 * @since 1/11/2020
 * @email chriseteka@gmail.com
 */
@Component
public class GenerateReport {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StockCategoryRepository stockCategoryRepository;

    @Autowired
    private WarehouseStockRepository warehouseStockRepository;

    @Autowired
    private ShopStocksRepository shopStocksRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ReturnedStockRepository returnedStockRepository;

    @Autowired
    private WaybillInvoiceRepository waybillInvoiceRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private EventLogRepository logRepository;

    public byte[] generate(BusinessOwner businessOwner, Date lastReportDate, Date nextReportDate, String notice){

        String businessOwnerMail = businessOwner.getBusinessOwnerEmail();
        System.out.println("Business Owner Email: " + businessOwnerMail);

        List<Warehouse> businessOwnerWarehouses = new ArrayList<>(Collections.emptyList());
        List<Shop> businessOwnerShops = new ArrayList<>(Collections.emptyList());
        List<Seller> businessOwnerStaff = new ArrayList<>(Collections.emptyList());
        List<StockCategory> businessOwnerStockCats = new ArrayList<>(Collections.emptyList());
        List<WarehouseStocks> warehouseStocksList = new ArrayList<>(Collections.emptyList());
        List<Invoice> todayInvoices = new ArrayList<>(Collections.emptyList());
        List<ShopStocks> shopStocksList = new ArrayList<>(Collections.emptyList());
        List<WaybillInvoice> waybillInvoices = new ArrayList<>(Collections.emptyList());

        businessOwnerWarehouses.addAll(warehouseRepository.findAllByCreatedBy(businessOwnerMail));
        businessOwnerShops.addAll(shopRepository.findAllByCreatedBy(businessOwnerMail));
        businessOwnerStaff.addAll(sellerRepository.findAllByCreatedBy(businessOwnerMail));

        long availableWarehouseStock = 0L;
        long numberOfNewStockInWarehouses = 0L;
        long availableShopStock = 0L;
        long numberOfNewStockInShops = 0L;
        long totalStockSoldToday = 0L;
        long totalStockReturnedToday = 0L;
        BigDecimal todayExpenses = BigDecimal.ZERO;
        BigDecimal todayIncome = BigDecimal.ZERO;
        BigDecimal todayDebts = BigDecimal.ZERO;

        if (!businessOwnerStaff.isEmpty()) {
            List<String> staffEmailList = businessOwnerStaff
                    .stream()
                    .map(Seller::getSellerEmail)
                    .collect(Collectors.toList());
            staffEmailList.add(businessOwnerMail);
            businessOwnerStockCats.addAll(staffEmailList
                    .stream()
                    .map(stockCategoryRepository::findAllByCreatedBy)
                    .flatMap(List::parallelStream)
                    .collect(Collectors.toList()));
            todayInvoices.addAll(staffEmailList
                    .stream()
                    .map(invoiceRepository::findAllByCreatedBy)
                    .flatMap(List::stream)
                    .filter(object -> getDateDifferenceInDays(lastReportDate, object.getCreatedDate()) >= 0
                            && getDateDifferenceInDays(nextReportDate, object.getCreatedDate()) <= 0)
                    .collect(Collectors.toList()));
            totalStockSoldToday = (todayInvoices
                    .stream()
                    .map(Invoice::getStockSold)
                    .flatMap(Set::stream)
                    .filter(object -> getDateDifferenceInDays(lastReportDate, object.getCreatedDate()) >= 0
                            && getDateDifferenceInDays(nextReportDate, object.getCreatedDate()) <= 0)
                    .map(StockSold::getQuantitySold)
                    .reduce(0, Integer::sum));
            totalStockReturnedToday = (staffEmailList
                    .stream()
                    .map(returnedStockRepository::findAllByCreatedBy)
                    .flatMap(List::stream)
                    .filter(object -> getDateDifferenceInDays(lastReportDate, object.getCreatedDate()) >= 0
                            && getDateDifferenceInDays(nextReportDate, object.getCreatedDate()) <= 0)
                    .map(ReturnedStock::getQuantityReturned)
                    .reduce(0, Integer::sum));
            waybillInvoices.addAll(staffEmailList
                    .stream()
                    .map(waybillInvoiceRepository::findAllByCreatedBy)
                    .flatMap(List::stream)
                    .filter(object -> getDateDifferenceInDays(lastReportDate, object.getCreatedDate()) >= 0
                            && getDateDifferenceInDays(nextReportDate, object.getCreatedDate()) <= 0)
                    .collect(Collectors.toList()));
            todayExpenses = (staffEmailList
                    .stream()
                    .map(expenseRepository::findAllByCreatedBy)
                    .flatMap(List::stream)
                    .filter(object -> getDateDifferenceInDays(lastReportDate, object.getCreatedDate()) >= 0
                            && getDateDifferenceInDays(nextReportDate, object.getCreatedDate()) <= 0)
                    .map(Expense::getExpenseAmount)
                    .reduce(todayExpenses, BigDecimal::add));
            todayIncome = (staffEmailList
                    .stream()
                    .map(incomeRepository::findAllByCreatedBy)
                    .flatMap(List::stream)
                    .filter(object -> getDateDifferenceInDays(lastReportDate, object.getCreatedDate()) >= 0
                            && getDateDifferenceInDays(nextReportDate, object.getCreatedDate()) <= 0)
                    .map(Income::getIncomeAmount)
                    .reduce(todayIncome, BigDecimal::add));
            todayDebts = (todayInvoices
                    .stream()
                    .map(Invoice::getDebt)
                    .reduce(todayDebts, BigDecimal::add));
        }
        if (!businessOwnerWarehouses.isEmpty()) {
            warehouseStocksList.addAll(businessOwnerWarehouses
                    .stream()
                    .map(warehouseStockRepository::findAllByWarehouse)
                    .flatMap(List::parallelStream)
                    .collect(Collectors.toList()));
            availableWarehouseStock = availableWarehouseStock + (warehouseStocksList
                    .stream()
                    .filter(warehouseStocks -> warehouseStocks.getStockQuantityRemaining() > 1)
                    .count());
            numberOfNewStockInWarehouses = numberOfNewStockInWarehouses + (warehouseStocksList
                    .stream()
                    .filter(object -> getDateDifferenceInDays(lastReportDate, object.getCreatedDate()) >= 0
                            && getDateDifferenceInDays(nextReportDate, object.getCreatedDate()) <= 0)
                    .count());
        }
        if (!businessOwnerShops.isEmpty()) {
            shopStocksList.addAll(businessOwnerShops
                    .stream()
                    .map(shopStocksRepository::findAllByShop)
                    .flatMap(List::parallelStream)
                    .collect(Collectors.toList()));
            availableShopStock = availableShopStock + (shopStocksList
                    .stream()
                    .filter(shopStock -> shopStock.getStockQuantityRemaining() > 1)
                    .count());
            numberOfNewStockInShops = numberOfNewStockInShops + (shopStocksList
                    .stream()
                    .filter(object -> getDateDifferenceInDays(lastReportDate, object.getCreatedDate()) >= 0
                            && getDateDifferenceInDays(nextReportDate, object.getCreatedDate()) <= 0)
                    .count());
        }

        Report summaryReport = new Report();

        //Set Preliminaries
        summaryReport.setBusinessName(businessOwner.getBusinessName());
        summaryReport.setTitle(notice);
        summaryReport.setTotalWarehouses(businessOwnerWarehouses.size());
        summaryReport.setTotalShops(businessOwnerShops.size());
        summaryReport.setTotalNumberOfRegisteredStaff(businessOwnerStaff.size());
        summaryReport.setTotalNumberOfStockCategory(businessOwnerStockCats.size());
        summaryReport.setTotalRegisteredStockInWarehouses(warehouseStocksList.size());
        summaryReport.setTotalRegisteredStockInShops(shopStocksList.size());
        summaryReport.setTotalStockAvailableInWarehouses(Math.toIntExact(availableWarehouseStock));
        summaryReport.setTotalStockAvailableInShops(Math.toIntExact(availableShopStock));
        summaryReport.setTotalNumberOfNewStockAddedToWarehouse(Math.toIntExact(numberOfNewStockInWarehouses));
        summaryReport.setTotalNumberOfNewStockAddedToShop(Math.toIntExact(numberOfNewStockInShops));
        summaryReport.setTotalNumberOfStockSold(Math.toIntExact(totalStockSoldToday));
        summaryReport.setTotalNumberOfSalesInvoices(todayInvoices.size());
        summaryReport.setTotalNumberOfStockReturned(Math.toIntExact(totalStockReturnedToday));
        summaryReport.setTotalNumberOfWareBillInvoices(waybillInvoices.size());
        summaryReport.setTotalIncurredExpenses(todayExpenses);
        summaryReport.setTotalIncomeMade(todayIncome);
        summaryReport.setTotalDebtsRegistered(todayDebts);
        summaryReport.setTotalEstimatedCashAtHand(todayIncome.subtract(todayExpenses));

        DecimalFormat formatter = new DecimalFormat("#, ###.00");

        PDFMap pdfMap = new PDFMap();
        pdfMap.setTitle(notice);
        pdfMap.setTableHead(new ArrayList<>(Arrays.asList("TITLE", "VALUE")));
        pdfMap.setTableData(
            Stream.of(
                new ArrayList<>(Arrays
                    .asList(new Tuple("Generated Report Title: ", summaryReport.getTitle()),
                            new Tuple("Business Name: ", summaryReport.getBusinessName()),
                            new Tuple("Number of Warehouses: ", String.valueOf(summaryReport.getTotalWarehouses())),
                            new Tuple("Number of Shops: ", String.valueOf(summaryReport.getTotalShops())),
                            new Tuple("Number of Staff: ", String.valueOf(summaryReport.getTotalNumberOfRegisteredStaff())),
                            new Tuple("Number of Stock Category: ", String.valueOf(summaryReport.getTotalNumberOfStockCategory())),
                            new Tuple("Number of Registered Stock in Warehouse: ", String.valueOf(summaryReport.getTotalRegisteredStockInWarehouses())),
                            new Tuple("Number of Registered Stock in Shop: ", String.valueOf(summaryReport.getTotalRegisteredStockInShops())),
                            new Tuple("Number of Available Stock in Warehouse: ", String.valueOf(summaryReport.getTotalStockAvailableInWarehouses())),
                            new Tuple("Number of Available Stock in Shop: ", String.valueOf(summaryReport.getTotalStockAvailableInShops())),
                            new Tuple("Number of New Stock Added to Warehouse: ", String.valueOf(summaryReport.getTotalNumberOfNewStockAddedToWarehouse())),
                            new Tuple("Number of New Stock Added to Shop: ", String.valueOf(summaryReport.getTotalNumberOfNewStockAddedToShop())),
                            new Tuple("Number of Stock Sold: ", String.valueOf(summaryReport.getTotalNumberOfStockSold())),
                            new Tuple("Number of Sales Invoice: ", String.valueOf(summaryReport.getTotalNumberOfSalesInvoices())),
                            new Tuple("Number of Stock Returned: ", String.valueOf(summaryReport.getTotalNumberOfStockReturned())),
                            new Tuple("Number of Ware Bill Invoices: ", String.valueOf(summaryReport.getTotalNumberOfWareBillInvoices())),
                            new Tuple("Total Incurred Expenses: ", formatter.format(summaryReport.getTotalIncurredExpenses())),
                            new Tuple("Total Income: ", formatter.format(summaryReport.getTotalIncomeMade())),
                            new Tuple("Total Debts: ", formatter.format(summaryReport.getTotalDebtsRegistered())),
                            new Tuple("Estimated Cash At Hand: ", formatter.format(summaryReport.getTotalEstimatedCashAtHand()))
                    ))
            )
            .flatMap(List::stream)
            .collect(Collectors.toList())
        );

        return GeneratePDFReport.generatePDFReport(pdfMap);
//        byte[] pdfReport = GeneratePDFReport.generatePDFReport(pdfMap);
//
//        OutputStream out = null;
//        try {
//            out = new FileOutputStream("out.pdf");
//            out.write(pdfReport);
//            out.close();
//            String m = new String(out)
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//
//        return Objects.requireNonNull(out).toString().getBytes();
    }

}

@Data
@AllArgsConstructor
class Tuple {

    String TITLE;

    String VALUE;
}
