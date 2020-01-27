package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.EXPENSE_TYPE;
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

    private List<Invoice> endOfDayInvoices = new ArrayList<>(Collections.emptyList());

    private List<Income> endOfDayIncome = new ArrayList<>(Collections.emptyList());

    private List<Expense> endOfDayExpenses = new ArrayList<>(Collections.emptyList());

    private List<SalesDiscount> endOfDayDiscounts = new ArrayList<>(Collections.emptyList());

    private List<StockSalesEndOfDay> endOfDayStockSales = new ArrayList<>(Collections.emptyList());

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

        List<Invoice> sortedInvoiceList = endOfDayInvoices
                .stream()
                .filter(object -> getDateDifferenceInDays(from, object.getCreatedDate()) >= 0
                        && getDateDifferenceInDays(to, object.getCreatedDate()) <= 0)
                .collect(Collectors.toList());

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

        List<StockSalesEndOfDay> uniqueStockSoldReport = stockNames
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

        CashFlowEndOfDay cashFlow = new CashFlowEndOfDay();

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

        //Sort all income by their types and sum them
        for (INCOME_TYPE income_type : INCOME_TYPE.values()){

            IncomeBreakDown incomeBreakDown = new IncomeBreakDown(income_type.toString(), BigDecimal.ZERO);
            cashFlow.getIncomeBreakDownList().addAll(sortedIncomeList
                .stream()
                .map(income -> {
                    if (income.getIncomeType().equals(income_type))
                        incomeBreakDown.setTotalAmount(incomeBreakDown.getTotalAmount()
                            .add(income.getIncomeAmount()));
                    return incomeBreakDown;
                })
                .collect(Collectors.toList()));
        }

        //Sort all expense by their types and sum them
        for (EXPENSE_TYPE expense_type : EXPENSE_TYPE.values()){

            ExpenseBreakDown expenseBreakDown = new ExpenseBreakDown(expense_type.toString(), BigDecimal.ZERO);
            cashFlow.expenseBreakDownList.addAll(sortedExpenseList
                .stream()
                .map(expense -> {
                    if (expense.getExpenseType().equals(expense_type))
                        expenseBreakDown.setTotalAmount(expenseBreakDown.getTotalAmount()
                            .add(expense.getExpenseAmount()));
                    return expenseBreakDown;
                })
                .collect(Collectors.toList()));
        }

        cashFlow.setTotalIncome(sortedIncomeList
            .stream()
            .map(Income::getIncomeAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        cashFlow.setTotalExpenses(sortedExpenseList
            .stream()
            .map(Expense::getExpenseAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        cashFlow.setTotalCashAtHand(cashFlow.getTotalIncome().subtract(cashFlow.getTotalExpenses()));

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

        List<String> DISCOUNT_TYPES = sortedDiscountList
            .stream()
            .map(SalesDiscount::getDiscountType)
            .collect(Collectors.toList());

        //Sort all discount by their types and sort them.
        for (String discountType : DISCOUNT_TYPES){

            DiscountBreakDown discountBreakDown = new DiscountBreakDown(discountType, BigDecimal.ZERO);
            balanceAccount.getDiscountBreakDownList().addAll(sortedDiscountList
                .stream()
                .map(salesDiscount -> {
                    if (salesDiscount.getDiscountType().equalsIgnoreCase(discountType))
                        discountBreakDown.setTotalAmount(discountBreakDown.getTotalAmount()
                            .add(salesDiscount.getDiscountAmount()));
                    return discountBreakDown;
                })
                .collect(Collectors.toList()));
        }

        //Discount might be error pruned
        balanceAccount.setTotalDiscountGiven(sortedDiscountList
            .stream()
            .map(SalesDiscount::getDiscountAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

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

        return endOfDayReport;
    }

    @Data
    class StockSalesEndOfDay {

        String stockSoldCategory;

        String stockSoldName;

        int stockSoldTotalQuantity;

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
