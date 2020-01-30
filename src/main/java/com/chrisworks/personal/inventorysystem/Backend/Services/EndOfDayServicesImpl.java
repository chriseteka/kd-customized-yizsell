package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
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

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.getDateDifferenceInDays;
import static ir.cafebabe.math.utils.BigDecimalUtils.is;

@Service
public class EndOfDayServicesImpl implements EndOfDayServices {

    private Date fromDate = null;

    private Date toDate = null;

    private final InvoiceRepository invoiceRepository;

    private final IncomeRepository incomeRepository;

    private final ExpenseRepository expenseRepository;

    private final SalesDiscountRepository discountRepository;

    private final SellerRepository sellerRepository;

    @Autowired
    public EndOfDayServicesImpl(InvoiceRepository invoiceRepository, IncomeRepository incomeRepository,
                                ExpenseRepository expenseRepository, SalesDiscountRepository discountRepository,
                                SellerRepository sellerRepository) {
        this.invoiceRepository = invoiceRepository;
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.discountRepository = discountRepository;
        this.sellerRepository = sellerRepository;
    }

    @Override
    public EndOfDayServicesImpl.EndOfDayReport generateEndOfDayReport() {

        if (AuthenticatedUserDetails.getAccount_type() == null
                || AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        if (fromDate == null) fromDate = new Date();
        if (toDate == null) toDate = fromDate;

        String loggedInUser = AuthenticatedUserDetails.getUserFullName();

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER))
            return computeEndOfDay(fromDate, toDate, Collections.singletonList(loggedInUser));

        else if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

            List<Seller> sellerList = sellerRepository.findAllByCreatedBy(loggedInUser);

            List<String> sellerEmails = sellerList
                .stream()
                .map(Seller::getSellerEmail)
                .collect(Collectors.toList());
            sellerEmails.add(loggedInUser);

            return computeEndOfDay(fromDate, toDate, sellerEmails);
        }

        else throw new InventoryAPIOperationException("Operation not allowed",
                    "Cannot verify the user making this request", null);

    }

    @Override
    public EndOfDayServicesImpl.EndOfDayReport generateEndOfDayReportFor(Date anyDate) {

        if (anyDate == null) throw new InventoryAPIOperationException("Not allowed",
                "Please pass in a valid date to compute end of day", null);

        fromDate = anyDate;

        return this.generateEndOfDayReport();

    }

    @Override
    public EndOfDayServicesImpl.EndOfDayReport generateEndOfDayReportBetween(Date from, Date to) {

        if (from == null || to == null) throw new InventoryAPIOperationException("Not allowed",
                "Please pass in a valid date to compute end of day, between given date intervals", null);

        fromDate = from;
        toDate = to;

        return this.generateEndOfDayReport();

    }

    private EndOfDayServicesImpl.EndOfDayReport computeEndOfDay(Date from, Date to, List<String> staff){

        //CLEAR ALL LISTS BEFORE COMPUTATION STARTS
        List<Invoice> endOfDayInvoices = new ArrayList<>(Collections.emptyList());

        List<Income> endOfDayIncome = new ArrayList<>(Collections.emptyList());

        List<Expense> endOfDayExpenses = new ArrayList<>(Collections.emptyList());

        List<SalesDiscount> endOfDayDiscounts = new ArrayList<>(Collections.emptyList());

        //Fetch All Needed Data from the database (invoices, income, expenses, discount).
        endOfDayInvoices.addAll(staff
            .stream()
            .map(invoiceRepository::findAllByCreatedBy)
            .flatMap(List::parallelStream)
            .collect(Collectors.toList())
        );
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

        //Sort invoices, income, expenses and discount by the date passed, default date is today.
        List<Invoice> sortedInvoiceList = endOfDayInvoices
                .stream()
                .filter(object -> getDateDifferenceInDays(from, object.getCreatedDate()) >= 0
                        && getDateDifferenceInDays(to, object.getCreatedDate()) <= 0)
                .collect(Collectors.toList());

        List<Income> sortedIncomeList = endOfDayIncome
                .stream()
                .filter(object -> getDateDifferenceInDays(from, object.getCreatedDate()) >= 0
                        && getDateDifferenceInDays(to, object.getCreatedDate()) <= 0)
                .collect(Collectors.toList());

        List<Expense> sortedExpenseList = endOfDayExpenses
                .stream()
                .filter(object -> getDateDifferenceInDays(from, object.getCreatedDate()) >= 0
                        && getDateDifferenceInDays(to, object.getCreatedDate()) <= 0)
                .collect(Collectors.toList());

        List<SalesDiscount> sortedDiscountList = endOfDayDiscounts
                .stream()
                .filter(object -> getDateDifferenceInDays(from, object.getCreatedDate()) >= 0
                        && getDateDifferenceInDays(to, object.getCreatedDate()) <= 0)
                .collect(Collectors.toList());

        //Compute the end of day report for stock sold.
        List<StockSalesEndOfDay> uniqueStockSoldReport = computeEndOfDayInvoices(sortedInvoiceList);

        //Compute the end of day for expenses incurred
        List<ExpenseBreakDown> expenseBreakDownList = computeEndOfDayExpenses(sortedExpenseList);

        //Compute the end of day for income made
        List<IncomeBreakDown> incomeBreakDownList = computeEndOfDayIncome(sortedIncomeList);

        //Compute the end of day for discounts given
        List<DiscountBreakDown> discountBreakDownList = computeEndOfDayDiscounts(sortedDiscountList);

        CashFlowEndOfDay cashFlow = new CashFlowEndOfDay();

        cashFlow.setIncomeBreakDownList(incomeBreakDownList);

        cashFlow.setExpenseBreakDownList(expenseBreakDownList);

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

        balanceAccount.setStockSoldTotalWorth(uniqueStockSoldReport
            .stream()
            .map(StockSalesEndOfDay::getWorthOfStockSold)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        //Add any other type of income that is not of stock_sale.
        balanceAccount.setOtherIncome(sortedIncomeList
            .stream()
            .filter(income -> !income.getIncomeType().equals(INCOME_TYPE.STOCK_SALE))
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
            .add(balanceAccount.otherIncome)
            .subtract(debits));

        EndOfDayReport endOfDayReport = new EndOfDayReport();

        endOfDayReport.setStockReportList(uniqueStockSoldReport);
        endOfDayReport.setCashFlow(cashFlow);
        endOfDayReport.setBalanceAccount(balanceAccount);
        endOfDayReport.setAccountStatus(is(balanceAccount.getTotalCashAtHand()).eq(cashFlow.getTotalCashAtHand())
            ? ACCOUNT_STATUS.BALANCED
            : ACCOUNT_STATUS.NOT_BALANCED);

        fromDate = null;
        toDate = null;

        return endOfDayReport;
    }

    private List<DiscountBreakDown> computeEndOfDayDiscounts(List<SalesDiscount> sortedDiscountList) {

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

    private List<ExpenseBreakDown> computeEndOfDayExpenses(List<Expense> sortedExpenseList) {

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
    class EndOfDayReport{

        List<StockSalesEndOfDay> stockReportList;

        CashFlowEndOfDay cashFlow;

        BalanceAccount balanceAccount;

        ACCOUNT_STATUS accountStatus;
    }
}
