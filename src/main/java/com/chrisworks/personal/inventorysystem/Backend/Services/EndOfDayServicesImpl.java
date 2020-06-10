package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.EOD_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.INCOME_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.*;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.getDateDifferenceInDays;
import static ir.cafebabe.math.utils.BigDecimalUtils.is;

@Service
public class EndOfDayServicesImpl implements EndOfDayServices {

    private final InvoiceRepository invoiceRepository;

    private final IncomeRepository incomeRepository;

    private final ExpenseRepository expenseRepository;

    private final SalesDiscountRepository discountRepository;

    private final SellerRepository sellerRepository;

    private final WarehouseStockRepository warehouseStockRepository;

    private final WarehouseRepository warehouseRepository;

    private final WaybillInvoiceRepository waybillInvoiceRepository;

    @Autowired
    public EndOfDayServicesImpl(InvoiceRepository invoiceRepository, IncomeRepository incomeRepository,
                                ExpenseRepository expenseRepository, SalesDiscountRepository discountRepository,
                                SellerRepository sellerRepository, WarehouseStockRepository warehouseStockRepository,
                                WarehouseRepository warehouseRepository, WaybillInvoiceRepository waybillInvoiceRepository) {
        this.invoiceRepository = invoiceRepository;
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.discountRepository = discountRepository;
        this.sellerRepository = sellerRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.warehouseRepository = warehouseRepository;
        this.waybillInvoiceRepository = waybillInvoiceRepository;
    }

    @Override
    public EndOfDayServicesImpl.EndOfDayReport generateEndOfDayReport(EOD_TYPE eod_type, Date fromDate, Date toDate) {

        if (AuthenticatedUserDetails.getAccount_type() == null)
            throw new InventoryAPIOperationException("Not allowed",
                    "Logged in user is unknown and hence not allowed to perform this operation", null);

        if (fromDate == null) fromDate = new Date();
        if (toDate == null) toDate = fromDate;

        String loggedInUser = AuthenticatedUserDetails.getUserFullName();

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT)) {

            Seller seller = sellerRepository.findDistinctBySellerEmail(loggedInUser);
            List<WarehouseAttendantEndOfDay> attendantEndOfDayList =
                computeWarehouseAttendantEndOfDay(fromDate, toDate, seller.getCreatedBy());

            return new EndOfDayServicesImpl.EndOfDayReport(attendantEndOfDayList);
        }

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER))
            return computeEndOfDay(eod_type, fromDate, toDate, Collections.singletonList(loggedInUser));

        else if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

            List<Seller> sellerList = sellerRepository.findAllByCreatedBy(loggedInUser);

            List<String> sellerEmails = sellerList
                .stream()
                .map(Seller::getSellerEmail)
                .collect(Collectors.toList());
            sellerEmails.add(loggedInUser);

            return computeEndOfDay(eod_type, fromDate, toDate, sellerEmails);
        }

        else throw new InventoryAPIOperationException("Operation not allowed",
                    "Cannot verify the user making this request", null);

    }

    @Override
    public EndOfDayServicesImpl.EndOfDayReport generateEndOfDayReportFor(EOD_TYPE eod_type, Date anyDate) {

        if (anyDate == null) throw new InventoryAPIOperationException("Not allowed",
                "Please pass in a valid date to compute end of day", null);

        return this.generateEndOfDayReport(eod_type, anyDate, null);

    }

    @Override
    public EndOfDayServicesImpl.EndOfDayReport generateEndOfDayReportBetween(EOD_TYPE eod_type, Date from, Date to) {

        if (from == null || to == null) throw new InventoryAPIOperationException("Not allowed",
                "Please pass in a valid date to compute end of day, between given date intervals", null);

        return this.generateEndOfDayReport(eod_type, from, to);

    }

    private EndOfDayServicesImpl.EndOfDayReport computeEndOfDay(EOD_TYPE eod_type, Date from, Date to, List<String> staff){

        //CLEAR ALL LISTS BEFORE COMPUTATION STARTS
        List<Invoice> endOfDayInvoices = new ArrayList<>(Collections.emptyList());

        List<Income> endOfDayIncome = new ArrayList<>(Collections.emptyList());

        List<Expense> endOfDayExpenses = new ArrayList<>(Collections.emptyList());

        List<SalesDiscount> endOfDayDiscounts = new ArrayList<>(Collections.emptyList());

        List<WarehouseAttendantEndOfDay> attendantEndOfDay = new ArrayList<>(Collections.emptyList());

        List<Invoice> sortedInvoiceList = new ArrayList<>(Collections.emptyList());

        List<Income> sortedIncomeList = new ArrayList<>(Collections.emptyList());

        List<Expense> sortedExpenseList = new ArrayList<>(Collections.emptyList());

        List<SalesDiscount> sortedDiscountList = new ArrayList<>(Collections.emptyList());

        List<StockSalesEndOfDay> uniqueStockSoldReport = new ArrayList<>(Collections.emptyList());

        //Fetch All Needed Data from the database (invoices, income, expenses, discount).
        if (!eod_type.equals(EOD_TYPE.WAREHOUSE_REPORTS_ONLY))
            endOfDayInvoices.addAll(staff
                .stream()
                .map(invoiceRepository::findAllByCreatedBy)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList())
            );
        if (eod_type.equals(EOD_TYPE.COMPLETE) || eod_type.equals(EOD_TYPE.CASH_ONLY)) {
            endOfDayIncome.addAll(staff
                    .stream()
                    .map(incomeRepository::findAllByCreatedBy)
                    .flatMap(List::parallelStream)
                    .collect(Collectors.toList())
            );
            endOfDayExpenses.addAll(staff
                    .stream()
                    .map(expenseRepository::findAllByCreatedBy)
                    .flatMap(List::parallelStream)
                    .collect(Collectors.toList())
            );
            endOfDayDiscounts.addAll(staff
                    .stream()
                    .map(discountRepository::findAllByCreatedBy)
                    .flatMap(List::parallelStream)
                    .collect(Collectors.toList())
            );
        }

        //Sort invoices, income, expenses and discount by the date passed, default date is today.
        if (!endOfDayInvoices.isEmpty()) {
            sortedInvoiceList.addAll(endOfDayInvoices
                    .stream()
                    .filter(object -> getDateDifferenceInDays(from, object.getCreatedDate()) >= 0
                            && getDateDifferenceInDays(to, object.getCreatedDate()) <= 0)
                    .collect(Collectors.toList()));
        }

        if (!endOfDayIncome.isEmpty()) {
            sortedIncomeList.addAll(endOfDayIncome
                    .stream()
                    .filter(object -> getDateDifferenceInDays(from, object.getCreatedDate()) >= 0
                            && getDateDifferenceInDays(to, object.getCreatedDate()) <= 0)
                    .collect(Collectors.toList()));
        }

        if (!endOfDayExpenses.isEmpty()) {
            sortedExpenseList.addAll(endOfDayExpenses
                    .stream()
                    .filter(object -> getDateDifferenceInDays(from, object.getCreatedDate()) >= 0
                            && getDateDifferenceInDays(to, object.getCreatedDate()) <= 0)
                    .collect(Collectors.toList()));
        }

        if (!endOfDayDiscounts.isEmpty()){
            sortedDiscountList.addAll(endOfDayDiscounts
                    .stream()
                    .filter(object -> getDateDifferenceInDays(from, object.getCreatedDate()) >= 0
                            && getDateDifferenceInDays(to, object.getCreatedDate()) <= 0)
                    .collect(Collectors.toList()));
        }

        //Compute the end of day report for stock sold.
        if (eod_type.equals(EOD_TYPE.COMPLETE) || eod_type.equals(EOD_TYPE.SHOP_SALES_ONLY))
            uniqueStockSoldReport.addAll(computeEndOfDayInvoices(sortedInvoiceList));

        //Compute the end of day for expenses incurred
        List<ExpenseBreakDown> expenseBreakDownList = computeEndOfDayExpenses(sortedExpenseList);

        //Compute the end of day for income made
        List<IncomeBreakDown> incomeBreakDownList = computeEndOfDayIncome(sortedIncomeList);

        //Compute the end of day for discounts given
        List<DiscountBreakDown> discountBreakDownList = computeEndOfDayDiscounts(sortedDiscountList);

        //Compute the end of day income sorted by their payment modes
        List<PaymentBreakDown> paymentBreakDownList = computeEndOfDayPayments(sortedInvoiceList);

        CashFlowEndOfDay cashFlow = new CashFlowEndOfDay();

        cashFlow.setIncomeBreakDownList(incomeBreakDownList);

        cashFlow.setExpenseBreakDownList(expenseBreakDownList);

        cashFlow.setPaymentBreakDownList(paymentBreakDownList);

        cashFlow.setTotalChangesGiven(sortedInvoiceList
            .stream()
            .map(Invoice::getBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        cashFlow.setTotalIncome(sortedIncomeList
            .stream()
            .map(Income::getIncomeAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        cashFlow.setTotalExpenses(sortedExpenseList
            .stream()
            .map(Expense::getExpenseAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        cashFlow.setTotalCashAtHand(cashFlow.getTotalIncome()
                .subtract(cashFlow.getTotalExpenses()
                    .add(cashFlow.getTotalChangesGiven())));

        BalanceAccount balanceAccount = new BalanceAccount();

        balanceAccount.setStockSoldTotalWorth(sortedInvoiceList
            .stream()
            .map(Invoice::getInvoiceTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        //Add any other type of income that is not of stock_sale.
        balanceAccount.setOtherIncome(sortedIncomeList
            .stream()
            .filter(income -> income.getIncomeType().equals(INCOME_TYPE.OTHERS))
            .map(Income::getIncomeAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        balanceAccount.setDiscountBreakDownList(discountBreakDownList);

        BigDecimal negativeDiscount = sortedDiscountList
            .stream()
            .filter(salesDiscount -> !salesDiscount.getDiscountType().equalsIgnoreCase("OVER SALE DISCOUNT"))
            .map(SalesDiscount::getDiscountAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal positiveDiscount = sortedDiscountList
            .stream()
            .filter(salesDiscount -> salesDiscount.getDiscountType().equalsIgnoreCase("OVER SALE DISCOUNT"))
            .map(SalesDiscount::getDiscountAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        //Discount might be error pruned
        balanceAccount.setTotalDiscountGiven(positiveDiscount.subtract(negativeDiscount).abs());

        balanceAccount.setTotalDebtsIncurred(sortedInvoiceList
            .stream()
            .map(Invoice::getDebt)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        BigDecimal debits = cashFlow.getTotalExpenses()
            .add(balanceAccount.getTotalDiscountGiven())
            .add(balanceAccount.getTotalDebtsIncurred());

        balanceAccount.setTotalCashAtHand(balanceAccount.getStockSoldTotalWorth()
            .add(balanceAccount.getOtherIncome())
            .subtract(debits));

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)
            && (eod_type.equals(EOD_TYPE.COMPLETE) || eod_type.equals(EOD_TYPE.WAREHOUSE_REPORTS_ONLY))
            && AuthenticatedUserDetails.getHasWarehouse()) {
            attendantEndOfDay.addAll(computeWarehouseAttendantEndOfDay(from, to, AuthenticatedUserDetails.getUserFullName()));
        }

        EndOfDayReport endOfDayReport = new EndOfDayReport();

        endOfDayReport.setStockReportList(uniqueStockSoldReport);
        endOfDayReport.setCashFlow(cashFlow);
        endOfDayReport.setBalanceAccount(balanceAccount);
        endOfDayReport.setAccountStatus(is(balanceAccount.getTotalCashAtHand()).eq(cashFlow.getTotalCashAtHand())
            ? ACCOUNT_STATUS.BALANCED
            : ACCOUNT_STATUS.NOT_BALANCED);
        endOfDayReport.setWarehouseAttendantEndOfDayList(attendantEndOfDay);

        return endOfDayReport;
    }

    private List<WarehouseAttendantEndOfDay> computeWarehouseAttendantEndOfDay(Date from, Date to, String businessOwner){

        List<NewStockAddedToWarehouse> newStockList = new ArrayList<>(Collections.emptyList());

        List<WaybillEndOfDay> waybillEndOfDayList = new ArrayList<>(Collections.emptyList());

        List<WarehouseAttendantEndOfDay> attendantEndOfDayList = new ArrayList<>(Collections.emptyList());

        List<Warehouse> warehouseList = warehouseRepository.findAllByCreatedBy(businessOwner);

        for (Warehouse warehouse: warehouseList) {

            WarehouseAttendantEndOfDay attendantEndOfDay = new WarehouseAttendantEndOfDay();

            attendantEndOfDay.setWarehouseName(warehouse.getWarehouseName());

            newStockList.addAll(warehouseStockRepository
                .findAllByWarehouse(warehouse)
                .stream()
                .filter(object -> getDateDifferenceInDays(from, object.getCreatedDate()) >= 0
                        && getDateDifferenceInDays(to, object.getCreatedDate()) <= 0)
                .map(warehouseStocks -> {
                    NewStockAddedToWarehouse newStock = new NewStockAddedToWarehouse();
                    newStock.setStockName(warehouseStocks.getStockName());
                    newStock.setStockCategory(warehouseStocks.getStockCategory().getCategoryName());
                    newStock.setQuantity(warehouseStocks.getLastRestockQuantity());

                    return newStock;
                })
                .collect(Collectors.toList()));

            List<WaybillInvoice> waybillInvoiceList = waybillInvoiceRepository.findAllByWarehouse(warehouse)
                .stream()
                .filter(object -> object.getIsWaybillReceived() && getDateDifferenceInDays(from, object.getCreatedDate()) >= 0
                        && getDateDifferenceInDays(to, object.getCreatedDate()) <= 0)
                .collect(Collectors.toList());

            waybillEndOfDayList
                .addAll(waybillInvoiceList
                    .stream()
                    .map(waybillInvoice -> waybillInvoice.getWaybilledStocks()
                        .stream()
                        .map(waybilledStocks -> {

                            WaybillEndOfDay waybillEndOfDay = new WaybillEndOfDay();

                            waybillEndOfDay.setStockName(waybilledStocks.getStockName());
                            waybillEndOfDay.setStockCategory(waybilledStocks.getStockCategory());
                            waybillEndOfDay.setQuantityWaybilled(waybilledStocks.getQuantityWaybilled());
                            waybillEndOfDay.setWaybillActualCost(waybilledStocks.getPurchasePricePerStock()
                                    .multiply(BigDecimal.valueOf(waybilledStocks.getQuantityWaybilled())));
                            waybillEndOfDay.setWaybillSellingWorth(waybilledStocks.getSellingPricePerStock()
                                    .multiply(BigDecimal.valueOf(waybilledStocks.getQuantityWaybilled())));

                            return waybillEndOfDay;
                        })
                        .collect(Collectors.toList()))
                    .flatMap(List::parallelStream)
                    .collect(Collectors.toList()));

            List<String> stockNames = waybillEndOfDayList
                    .stream()
                    .map(WaybillEndOfDay::getStockName)
                    .distinct()
                    .collect(Collectors.toList());

            List<WaybillEndOfDay> sortedWaybillInvoiceList = stockNames
                .stream()
                .map(stockName -> waybillEndOfDayList
                    .stream()
                    .filter(waybillEndOfDay -> waybillEndOfDay.getStockName().equalsIgnoreCase(stockName))
                    .reduce(new WaybillEndOfDay(), (t1, t2) -> {

                        if (t1.getStockName() == null || StringUtils.isEmpty(t1.getStockName())) return t2;
                        if ((t1.getStockName().equalsIgnoreCase(t2.getStockName()))) {
                            t1.setQuantityWaybilled(t1.getQuantityWaybilled() + t2.getQuantityWaybilled());
                            t1.setWaybillActualCost(t1.getWaybillActualCost().add(t2.getWaybillActualCost()));
                            t1.setWaybillSellingWorth(t1.getWaybillSellingWorth().add(t2.getWaybillSellingWorth()));
                        }
                        return t1;
                    }))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


            attendantEndOfDay.setNewStockAddedToWarehouseList(newStockList);
            attendantEndOfDay.setWaybillEndOfDayList(sortedWaybillInvoiceList);
            attendantEndOfDay.setNumberOfWaybillInvoices(waybillInvoiceList.size());

            attendantEndOfDayList.add(attendantEndOfDay);
        }

        return attendantEndOfDayList;
    }

    private List<DiscountBreakDown> computeEndOfDayDiscounts(List<SalesDiscount> sortedDiscountList) {

        if (sortedDiscountList.isEmpty()) return Collections.emptyList();

        //Sort all expense by their types and sum them
        List<DiscountBreakDown> discountBreakDownList = new ArrayList<>(Collections.emptyList());
        discountBreakDownList.addAll(sortedDiscountList
            .stream()
            .map(discount -> {

                DiscountBreakDown discountBreakDown = new DiscountBreakDown();
                discountBreakDown.setDiscountType(discount.getDiscountType());
                discountBreakDown.setTotalAmount(discount.getDiscountAmount());

                return discountBreakDown;
            })
            .collect(Collectors.toList())
        );

        List<String> discountTypes = discountBreakDownList
                .stream()
                .map(DiscountBreakDown::getDiscountType)
                .distinct()
                .collect(Collectors.toList());

        return discountTypes
            .stream()
            .map(discount_type -> discountBreakDownList
                .stream()
                .filter(discount -> discount.getDiscountType().equalsIgnoreCase(discount_type))
                .reduce(new DiscountBreakDown(), (t1, t2) -> {

                    if (t1.getDiscountType() == null) return t2;
                    if ((t1.getDiscountType().equalsIgnoreCase(t2.getDiscountType())))
                        t1.setTotalAmount(t1.getTotalAmount().add(t2.getTotalAmount()));
                    return t1;
                }))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private List<IncomeBreakDown> computeEndOfDayIncome(List<Income> sortedIncomeList) {

        if (sortedIncomeList.isEmpty()) return Collections.emptyList();

        //Sort all expense by their types and sum them
        List<IncomeBreakDown> incomeBreakDownList = new ArrayList<>(Collections.emptyList());
        incomeBreakDownList.addAll(sortedIncomeList
            .stream()
            .map(income -> {

                IncomeBreakDown incomeBreakDown = new IncomeBreakDown();
                incomeBreakDown.setIncomeType(income.getIncomeType().toString());
                incomeBreakDown.setTotalAmount(income.getIncomeAmount());

                return incomeBreakDown;
            })
            .collect(Collectors.toList())
        );

        List<String> incomeTypes = incomeBreakDownList
                .stream()
                .map(IncomeBreakDown::getIncomeType)
                .distinct()
                .collect(Collectors.toList());

        return incomeTypes
            .stream()
            .map(income_type -> incomeBreakDownList
                .stream()
                .filter(income -> income.getIncomeType().equalsIgnoreCase(income_type))
                .reduce(new IncomeBreakDown(), (t1, t2) -> {

                    if (t1.getIncomeType() == null) return t2;
                    if ((t1.getIncomeType().equalsIgnoreCase(t2.getIncomeType())))
                        t1.setTotalAmount(t1.getTotalAmount().add(t2.getTotalAmount()));
                    return t1;
                }))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private List<PaymentBreakDown> computeEndOfDayPayments(List<Invoice> sortedInvoiceList) {

        if (sortedInvoiceList.isEmpty()) return Collections.emptyList();

        //Sort all expense by their types and sum them
        List<PaymentBreakDown> paymentBreakDownList = new ArrayList<>(Collections.emptyList());
        paymentBreakDownList.addAll(sortedInvoiceList
                .stream()
                .map(invoice -> {

                    PaymentBreakDown paymentBreakDown = new PaymentBreakDown();
                    paymentBreakDown.setPaymentType(invoice.getPaymentMode().toString());
                    paymentBreakDown.setTotalAmount(invoice.getAmountPaid());

                    return paymentBreakDown;
                })
                .collect(Collectors.toList())
        );

        List<String> paymentModes = paymentBreakDownList
                .stream()
                .map(PaymentBreakDown::getPaymentType)
                .distinct()
                .collect(Collectors.toList());

        return paymentModes
            .stream()
            .map(payment_type -> paymentBreakDownList
                .stream()
                .filter(payment -> payment.getPaymentType().equalsIgnoreCase(payment_type))
                .reduce(new PaymentBreakDown(), (t1, t2) -> {

                    if (t1.getPaymentType() == null) return t2;
                    if ((t1.getPaymentType().equalsIgnoreCase(t2.getPaymentType())))
                        t1.setTotalAmount(t1.getTotalAmount().add(t2.getTotalAmount()));
                    return t1;
                }))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private List<ExpenseBreakDown> computeEndOfDayExpenses(List<Expense> sortedExpenseList) {

        if (sortedExpenseList.isEmpty()) return Collections.emptyList();

        //Sort all expense by their types and sum them
        List<ExpenseBreakDown> expenseBreakdown = new ArrayList<>(Collections.emptyList());
            expenseBreakdown.addAll(sortedExpenseList
                .stream()
                .map(expense -> {

                    ExpenseBreakDown expenseBreakDown = new ExpenseBreakDown();
                    expenseBreakDown.setExpenseType(expense.getExpenseType().toString());
                    expenseBreakDown.setTotalAmount(expense.getExpenseAmount());

                    return expenseBreakDown;
                })
                .collect(Collectors.toList())
            );

        List<String> expenseTypes = expenseBreakdown
            .stream()
            .map(ExpenseBreakDown::getExpenseType)
            .distinct()
            .collect(Collectors.toList());

        return expenseTypes
            .stream()
            .map(expense_type -> expenseBreakdown
                .stream()
                .filter(expense -> expense.getExpenseType().equalsIgnoreCase(expense_type))
                .reduce(new ExpenseBreakDown(), (t1, t2) -> {

                    if (t1.getExpenseType() == null) return t2;
                    if ((t1.getExpenseType().equalsIgnoreCase(t2.getExpenseType())))
                        t1.setTotalAmount(t1.getTotalAmount().add(t2.getTotalAmount()));
                    return t1;
                }))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private List<StockSalesEndOfDay> computeEndOfDayInvoices(List<Invoice> sortedInvoiceList) {

        if (sortedInvoiceList.isEmpty()) return Collections.emptyList();

        List<StockSalesEndOfDay> endOfDayStockSales = new ArrayList<>(Collections.emptyList());

        endOfDayStockSales.addAll(sortedInvoiceList
            .stream()
            .map(Invoice::getStockSold)
            .flatMap(stockSoldSet -> stockSoldSet
                .stream()
                .map(stockSold -> {
                    StockSalesEndOfDay s = new StockSalesEndOfDay();
                    s.setStockSoldCategory(stockSold.getStockCategory());
                    s.setStockSoldName(stockSold.getStockName());
                    s.setStockSoldTotalQuantity(stockSold.getQuantitySold());
                    s.setWorthOfStockSold(stockSold.getPricePerStockSold()
                            .multiply(BigDecimal.valueOf(s.getStockSoldTotalQuantity())));
                    return s;
                }))
            .collect(Collectors.toList())
        );

        List<String> stockNames = endOfDayStockSales
            .stream()
            .map(StockSalesEndOfDay::getStockSoldName)
            .distinct()
            .collect(Collectors.toList());

        return stockNames
            .stream()
            .map(name -> endOfDayStockSales
                .stream()
                .filter(stock -> stock.getStockSoldName().equalsIgnoreCase(name))
                .reduce(new StockSalesEndOfDay(), (t1, t2) -> {

                    if (t1.getStockSoldName() == null) return t2;
                    if ((t1.getStockSoldName().equalsIgnoreCase(t2.getStockSoldName()))) {

                        t1.setStockSoldTotalQuantity(t1.getStockSoldTotalQuantity()
                                + t2.getStockSoldTotalQuantity());
                        t1.setWorthOfStockSold(t1.getWorthOfStockSold()
                                .add(t2.getWorthOfStockSold()));
                    }
                    return t1;
                }))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Data
    class StockSalesEndOfDay {

        String stockSoldCategory;

        String stockSoldName;

        int stockSoldTotalQuantity;

        int totalQuantityRemaining;

        BigDecimal worthOfStockSold;
    }

    @Data
    class NewStockAddedToWarehouse{

        String stockCategory;

        String stockName;

        int quantity;
    }

    @Data
    class WaybillEndOfDay{

        String stockCategory;

        String stockName;

        int quantityWaybilled;

        BigDecimal waybillActualCost;

        BigDecimal waybillSellingWorth;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    class IncomeBreakDown{

        String incomeType;

        BigDecimal totalAmount;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    class PaymentBreakDown{

        String paymentType;

        BigDecimal totalAmount;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    class ExpenseBreakDown{

        String expenseType;

        BigDecimal totalAmount;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    class DiscountBreakDown{

        String discountType;

        BigDecimal totalAmount;
    }

    @Data
    class CashFlowEndOfDay{

        List<IncomeBreakDown> incomeBreakDownList = new ArrayList<>(Collections.emptyList());

        List<ExpenseBreakDown> expenseBreakDownList = new ArrayList<>(Collections.emptyList());

        List<PaymentBreakDown> paymentBreakDownList = new ArrayList<>(Collections.emptyList());

        BigDecimal totalIncome = BigDecimal.ZERO;

        BigDecimal totalExpenses = BigDecimal.ZERO;

        BigDecimal totalChangesGiven = BigDecimal.ZERO;

        BigDecimal totalCashAtHand = BigDecimal.ZERO;
    }

    @Data
    class BalanceAccount{

        BigDecimal stockSoldTotalWorth = BigDecimal.ZERO;

        BigDecimal otherIncome = BigDecimal.ZERO;

        List<DiscountBreakDown> discountBreakDownList = new ArrayList<>(Collections.emptyList());

        BigDecimal totalDiscountGiven = BigDecimal.ZERO;

        BigDecimal totalDebtsIncurred = BigDecimal.ZERO;

        BigDecimal totalCashAtHand = BigDecimal.ZERO;
    }

    enum ACCOUNT_STATUS{

        BALANCED,

        NOT_BALANCED
    }

    @Data
    @NoArgsConstructor
    class EndOfDayReport{

        List<WarehouseAttendantEndOfDay> warehouseAttendantEndOfDayList;

        List<StockSalesEndOfDay> stockReportList;

        CashFlowEndOfDay cashFlow;

        BalanceAccount balanceAccount;

        ACCOUNT_STATUS accountStatus;

        private EndOfDayReport(List<WarehouseAttendantEndOfDay> attendantEndOfDayList) {
            this.warehouseAttendantEndOfDayList = attendantEndOfDayList;
        }
    }

    @Data
    class WarehouseAttendantEndOfDay{

        String warehouseName;

        int numberOfWaybillInvoices;

        List<NewStockAddedToWarehouse> newStockAddedToWarehouseList;

        List<WaybillEndOfDay> waybillEndOfDayList;
    }
}
