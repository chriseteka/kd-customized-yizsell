//package com.chrisworks.personal.inventorysystem.Backend.Utility.BackgroundJobs;
//
//import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Invoice;
//import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop;
//import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ShopStocks;
//import com.chrisworks.personal.inventorysystem.Backend.Repositories.InvoiceRepository;
//import com.chrisworks.personal.inventorysystem.Backend.Repositories.ShopStocksRepository;
//import com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices.UserMiniProfileRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicReference;
//import java.util.stream.Collectors;
//
//import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.stripStringOfExpiryDate;
//import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;
//
//@Component
//@RequiredArgsConstructor
//public class SingleExecJob {
//
//    private final InvoiceRepository invoiceRepository;
//    private final ShopStocksRepository shopStocksRepository;
//    private final UserMiniProfileRepository userMiniProfileRepository;
//
//    private static final long JOB_FIXED_TIME = 1000L;
//
//    @Scheduled(initialDelay = JOB_FIXED_TIME, fixedDelay = Long.MAX_VALUE)
//    private void sendDailySummaryToBusinessOwners(){
//
//        List<ShopStocks> stockList = shopStocksRepository.findAll();
//        List<Invoice> unProcessedInvoices = new ArrayList<>();
//
//        List<Invoice> processedInvoices = invoiceRepository.findAll().stream()
//            .peek(invoice -> {
//
//                String invoiceCreator = invoice.getCreatedBy();
//                AtomicReference<Map<Shop, Double>> shopMatchPercent = new AtomicReference<>(new HashMap<>());
//
//                if (invoice.getSeller() != null) invoice.setShop(invoice.getSeller().getShop());
//                else{
//                    double stockCount = (double) invoice.getStockSold().size();
//
//                    invoice.getStockSold().forEach(stockSold -> {
//                        List<ShopStocks> matchedStocks = stockList.stream()
//                            .filter(stock -> stock.getShop().getCreatedBy().equalsIgnoreCase(invoiceCreator))
//                            .filter(stock -> stockSold.getStockName().contains(stripStringOfExpiryDate(stock.getStockName()))
//                                || stock.getStockCategory().getCategoryName().contains(stockSold.getStockCategory()))
//                            .collect(Collectors.toList());
//
//                        if (matchedStocks.isEmpty()) {
//                            System.out.println("UNMATCHED INVOICE: " + invoice);
//                            unProcessedInvoices.add(invoice);
//                            return;
//                        }
//
//                        Map<Shop, Double> shopMatchRate = shopMatchPercent.get();
//                        Shop shop = matchedStocks.get(0).getShop();
//                        if (matchedStocks.size() == 1)
//                            shopMatchRate.put(shop, shopMatchRate.getOrDefault(shop, 0D) + ((1 / stockCount) * 100));
//                        else {
//                            Map<Shop, List<ShopStocks>> matchedShopStockMap = matchedStocks.stream()
//                                .collect(Collectors.groupingBy(ShopStocks::getShop));
//                            if (matchedShopStockMap.size() == 1)
//                                shopMatchRate.put(shop, shopMatchRate.getOrDefault(shop, 0D) + ((1 / stockCount) * 100));
//                            else matchedStocks.forEach(stock ->
//                                shopMatchRate.put(shop, shopMatchRate.getOrDefault(shop, 0D) + ((1 / stockCount) * 100)));
//                        }
//                    });
//
//                    invoice.setShop(shopMatchPercent.get().entrySet().stream()
//                        .sorted(Map.Entry.comparingByValue()).limit(1)
//                        .map(Map.Entry::getKey).collect(toSingleton()));
//                }
//                invoice.setSoldBy(userMiniProfileRepository.findDistinctByEmail(invoiceCreator));
//            }).collect(Collectors.toList());
//
//        List<Invoice> updatedInvoiceList = invoiceRepository.saveAll(processedInvoices);
//
//        long existingInvoicesCount = invoiceRepository.count();
//        long processedInvoicesCount = processedInvoices.stream().filter(invoice -> invoice.getSoldBy() != null && invoice.getShop() != null).count();
//
//        System.out.println("NUMBER OF EXISTING INVOICES: " + existingInvoicesCount);
//        System.out.println("NUMBER OF PROCESSED INVOICES: " + processedInvoicesCount);
//        System.out.println("NUMBER OF UNPROCESSED INVOICES: " + unProcessedInvoices.size());
//        System.out.println("NUMBER OF UPDATED INVOICES: " + updatedInvoiceList.size());
//        System.out.println("WERE ALL INVOICE CHECKED: " + (existingInvoicesCount == processedInvoicesCount));
//    }
//}
