package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.*;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;

/**
 * @author Chris_Eteka
 * @email chriseteka@gmail.com
 * @since 11/27/2019
 */
@Service
public class GenericServiceImpl implements GenericService {


    private SupplierRepository supplierRepository;

    private CustomerRepository customerRepository;

    private ExpenseRepository expenseRepository;

    private IncomeRepository incomeRepository;

    private SellerRepository sellerRepository;

    private WarehouseRepository warehouseRepository;

    private StockCategoryRepository stockCategoryRepository;

    private ShopRepository shopRepository;

    private SalesDiscountRepository salesDiscountRepository;

    private PromoSideService promoSideService;

    @Autowired
    public GenericServiceImpl
            (SupplierRepository supplierRepository, CustomerRepository customerRepository,
             ExpenseRepository expenseRepository, IncomeRepository incomeRepository,
             SellerRepository sellerRepository, WarehouseRepository warehouseRepository,
             StockCategoryRepository stockCategoryRepository, ShopRepository shopRepository,
             SalesDiscountRepository salesDiscountRepository, PromoSideService promoSideService) {
        this.supplierRepository = supplierRepository;
        this.customerRepository = customerRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.sellerRepository = sellerRepository;
        this.warehouseRepository = warehouseRepository;
        this.stockCategoryRepository = stockCategoryRepository;
        this.shopRepository = shopRepository;
        this.salesDiscountRepository = salesDiscountRepository;
        this.promoSideService = promoSideService;
    }

    @Override
    public Customer addCustomer(Customer customer) {

        if (null == customer) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find customer entity to save", null);

        List<Customer> customerFoundList = customerRepository
                .findAllByCustomerPhoneNumber(customer.getCustomerPhoneNumber());

        if (null == customerFoundList || customerFoundList.isEmpty()) {

            customer.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
            return customerRepository.save(customer);
        } else {

            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

                List<String> sellerNames = this.sellersByAuthUserId()
                        .stream()
                        .map(Seller::getSellerEmail)
                        .collect(Collectors.toList());
                sellerNames.add(AuthenticatedUserDetails.getUserFullName());

                boolean match = customerFoundList
                        .stream()
                        .map(Customer::getCreatedBy)
                        .anyMatch(sellerNames::contains);

                if (!match) {

                    customer.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
                    return customerRepository.save(customer);
                } else return customerFoundList
                        .stream()
                        .filter(customerFound -> customerFound.getCustomerPhoneNumber()
                                .equalsIgnoreCase(customer.getCustomerPhoneNumber())
                                && sellerNames.contains(customerFound.getCreatedBy()))
                        .collect(toSingleton());
            } else {

                Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());
                List<Seller> sellerList = sellerRepository.findAllByCreatedBy(seller.getCreatedBy());

                List<String> sellerNames = sellerList
                        .stream()
                        .map(Seller::getSellerEmail)
                        .collect(Collectors.toList());
                sellerNames.add(seller.getCreatedBy());

                boolean match = customerFoundList
                        .stream()
                        .map(Customer::getCreatedBy)
                        .anyMatch(sellerNames::contains);

                if (!match) {

                    customer.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
                    return customerRepository.save(customer);
                } else return customerFoundList
                        .stream()
                        .filter(customerFound -> customerFound.getCustomerPhoneNumber()
                                .equalsIgnoreCase(customer.getCustomerPhoneNumber())
                                && sellerNames.contains(customerFound.getCreatedBy()))
                        .collect(toSingleton());
            }
        }
    }

    @Override
    public Supplier addSupplier(Supplier supplier) {

        if (null == supplier) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find supplier entity to save", null);

        List<Supplier> supplierListFound = supplierRepository.findAllBySupplierPhoneNumber(supplier.getSupplierPhoneNumber());

        if (null == supplierListFound || supplierListFound.isEmpty()) {

            supplier.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
            return supplierRepository.save(supplier);
        } else {

            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

                List<String> sellerNames = this.sellersByAuthUserId()
                        .stream()
                        .map(Seller::getSellerEmail)
                        .collect(Collectors.toList());
                sellerNames.add(AuthenticatedUserDetails.getUserFullName());

                boolean match = supplierListFound.stream()
                        .map(Supplier::getCreatedBy)
                        .anyMatch(sellerNames::contains);

                if (!match) {

                    supplier.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
                    return supplierRepository.save(supplier);
                } else return supplierListFound
                        .stream()
                        .filter(supplierFound -> supplierFound.getSupplierPhoneNumber()
                                .equalsIgnoreCase(supplier.getSupplierPhoneNumber())
                                && sellerNames.contains(supplierFound.getCreatedBy()))
                        .collect(toSingleton());
            } else {

                Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());
                List<Seller> sellerList = sellerRepository.findAllByCreatedBy(seller.getCreatedBy());

                List<String> sellerNames = sellerList.stream()
                        .map(Seller::getSellerEmail)
                        .collect(Collectors.toList());
                sellerNames.add(seller.getCreatedBy());

                boolean match = supplierListFound
                        .stream()
                        .map(Supplier::getCreatedBy)
                        .anyMatch(sellerNames::contains);

                if (!match) {

                    supplier.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
                    return supplierRepository.save(supplier);
                } else return supplierListFound
                        .stream()
                        .filter(supplierFound -> supplierFound.getSupplierPhoneNumber()
                                .equalsIgnoreCase(supplier.getSupplierPhoneNumber())
                                && sellerNames.contains(supplierFound.getCreatedBy()))
                        .collect(toSingleton());
            }
        }
    }

    @Override
    public StockCategory addStockCategory(StockCategory stockCategory) {

        if (null == stockCategory) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find stockCategory entity to save", null);

        List<StockCategory> categoryListFound = stockCategoryRepository
                .findAllByCategoryName(stockCategory.getCategoryName());

        if (null == categoryListFound || categoryListFound.isEmpty()) {

            stockCategory.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
            return stockCategoryRepository.save(stockCategory);
        } else {

            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

                List<String> sellerNames = this.sellersByAuthUserId()
                        .stream()
                        .map(Seller::getSellerEmail)
                        .collect(Collectors.toList());
                sellerNames.add(AuthenticatedUserDetails.getUserFullName());

                boolean match = categoryListFound.stream()
                        .map(StockCategory::getCreatedBy)
                        .anyMatch(sellerNames::contains);

                if (!match) {

                    stockCategory.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
                    return stockCategoryRepository.save(stockCategory);
                } else return categoryListFound
                        .stream()
                        .filter(category -> category.getCategoryName()
                                .equalsIgnoreCase(stockCategory.getCategoryName())
                                && sellerNames.contains(category.getCreatedBy()))
                        .collect(toSingleton());
            } else {

                Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());
                List<Seller> sellerList = sellerRepository.findAllByCreatedBy(seller.getCreatedBy());

                List<String> sellerNames = sellerList.stream()
                        .map(Seller::getSellerEmail)
                        .collect(Collectors.toList());
                sellerNames.add(seller.getCreatedBy());

                boolean match = categoryListFound.stream()
                        .map(StockCategory::getCreatedBy)
                        .anyMatch(sellerNames::contains);

                if (!match) {

                    stockCategory.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
                    return stockCategoryRepository.save(stockCategory);
                } else return categoryListFound
                        .stream()
                        .filter(category -> category.getCategoryName()
                                .equalsIgnoreCase(stockCategory.getCategoryName())
                                && sellerNames.contains(category.getCreatedBy()))
                        .collect(toSingleton());
            }
        }
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

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER))
            expense.setShop(shopBySellerName(AuthenticatedUserDetails.getUserFullName()));

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

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER))
            income.setShop(shopBySellerName(AuthenticatedUserDetails.getUserFullName()));

        return incomeRepository.save(income);
    }

    @Override
    public Shop shopBySellerName(String sellerName) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        if (null == sellerName || sellerName.isEmpty()) throw new
                InventoryAPIOperationException("seller name error", "seller name is empty or null", null);

        Seller sellerFound = sellerRepository.findDistinctBySellerFullNameOrSellerEmail(sellerName, sellerName);

        if (null == sellerFound) throw new InventoryAPIResourceNotFoundException
                ("Seller not retrieved", "Seller with name: " + sellerName + " was not found.", null);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER) &&
                !sellerFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
            throw new InventoryAPIOperationException("Not allowed",
                    "You cannot view details of a seller not created by you", null);

        if (null == sellerFound.getShop()) throw new InventoryAPIResourceNotFoundException
                ("Seller does not have a shop", "Seller has not been assigned to any shop", null);

        return sellerFound.getShop();
    }

    @Override
    public Warehouse warehouseByWarehouseAttendantName(String warehouseAttendantName) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        if (null == warehouseAttendantName || warehouseAttendantName.isEmpty())
            throw new InventoryAPIOperationException("Warehouse attendant name error",
                    "Warehouse attendant name is empty or null", null);

        Seller sellerFound = sellerRepository
                .findDistinctBySellerFullNameOrSellerEmail(warehouseAttendantName, warehouseAttendantName);

        if (null == sellerFound) throw new InventoryAPIResourceNotFoundException("Warehouse attendant not retrieved",
                "Warehouse attendant with name: " + warehouseAttendantName + " was not found.", null);

        if (!sellerFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
            throw new InventoryAPIOperationException("Not allowed",
                    "You cannot view details of a warehouse attendant not created by you", null);

        if (null == sellerFound.getWarehouse()) throw new InventoryAPIResourceNotFoundException
                ("Seller does not have a warehouse", "Seller has not been assigned to any warehouse", null);

        return sellerFound.getWarehouse();
    }

    @Override
    public List<Warehouse> warehouseByAuthUserId() {

        Long authUserId = AuthenticatedUserDetails.getUserId();

        ACCOUNT_TYPE authUserType = AuthenticatedUserDetails.getAccount_type();

        String authUserMail = AuthenticatedUserDetails.getUserFullName();

        if (null == authUserId || authUserId < 0 || !authUserId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("authUserId id error", "Authenticated user id is empty or not a valid number", null);

        if (authUserType.equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            return warehouseRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());
        if (authUserType.equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT)) {

            Seller sellerFound = sellerRepository.findDistinctBySellerFullNameOrSellerEmail(authUserMail, authUserMail);
            return new ArrayList<>(Collections.singleton(sellerFound.getWarehouse()));
        }
        if (authUserType.equals(ACCOUNT_TYPE.SHOP_SELLER)) {

            Seller sellerFound = sellerRepository.findDistinctBySellerFullNameOrSellerEmail(authUserMail, authUserMail);
            return warehouseRepository.findAllByCreatedBy(sellerFound.getCreatedBy());
        }

        return null;
    }

    @Override
    public List<Shop> shopByAuthUserId() {

        Long authUserId = AuthenticatedUserDetails.getUserId();

        ACCOUNT_TYPE authUserType = AuthenticatedUserDetails.getAccount_type();

        String authUserMail = AuthenticatedUserDetails.getUserFullName();

        if (null == authUserId || authUserId < 0 || !authUserId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("authUserId id error", "Authenticated user id is empty or not a valid number", null);

        if (authUserType.equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            return shopRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());
        if (authUserType.equals(ACCOUNT_TYPE.SHOP_SELLER)) {

            Seller sellerFound = sellerRepository.findDistinctBySellerFullNameOrSellerEmail(authUserMail, authUserMail);
            return new ArrayList<>(Collections.singleton(sellerFound.getShop()));
        }

        return Collections.emptyList();
    }

    @Override
    public List<Seller> sellersByAuthUserId() {

        if (AuthenticatedUserDetails.getAccount_type() == null ||
                !AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return sellerRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());
    }

    @Override
    public Customer getAuthUserCustomerByPhoneNumber(String customerPhoneNumber) {

        List<Customer> customerFoundList = customerRepository
                .findAllByCustomerPhoneNumber(customerPhoneNumber);

        if (null == customerFoundList || customerFoundList.isEmpty()) throw new InventoryAPIResourceNotFoundException
                ("Customer not found", "Customer with phone number: " + customerPhoneNumber + " was not found", null);

        List<String> sellerNames;
        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            sellerNames = this.sellersByAuthUserId()
                    .stream()
                    .map(Seller::getSellerEmail)
                    .collect(Collectors.toList());
            sellerNames.add(AuthenticatedUserDetails.getUserFullName());
        }
        else {
            Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());
            List<Seller> sellerList = sellerRepository.findAllByCreatedBy(seller.getCreatedBy());

            sellerNames = sellerList
                    .stream()
                    .map(Seller::getSellerEmail)
                    .collect(Collectors.toList());
            sellerNames.add(seller.getCreatedBy());
        }
        return customerFoundList
                .stream()
                .filter(customerFound -> customerFound.getCustomerPhoneNumber().equalsIgnoreCase(customerPhoneNumber)
                        && sellerNames.contains(customerFound.getCreatedBy()))
                .collect(toSingleton());
    }

    @Override
    public Supplier getAuthUserSupplierByPhoneNumber(String supplierPhoneNumber) {

        List<Supplier> supplierFoundList = supplierRepository
                .findAllBySupplierPhoneNumber(supplierPhoneNumber);

        if (null == supplierFoundList || supplierFoundList.isEmpty()) throw new InventoryAPIResourceNotFoundException
                ("Supplier not found", "Supplier with phone number: " + supplierPhoneNumber + " was not found", null);

        List<String> sellerNames;
        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            sellerNames = this.sellersByAuthUserId()
                    .stream()
                    .map(Seller::getSellerEmail)
                    .collect(Collectors.toList());
            sellerNames.add(AuthenticatedUserDetails.getUserFullName());
        }
        else {
            Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());
            List<Seller> sellerList = sellerRepository.findAllByCreatedBy(seller.getCreatedBy());

            sellerNames = sellerList
                    .stream()
                    .map(Seller::getSellerEmail)
                    .collect(Collectors.toList());
            sellerNames.add(seller.getCreatedBy());
        }
        return supplierFoundList
                .stream()
                .filter(supplierFound -> supplierFound.getSupplierPhoneNumber().equalsIgnoreCase(supplierPhoneNumber)
                        && sellerNames.contains(supplierFound.getCreatedBy()))
                .collect(toSingleton());
    }

    @Override
    public StockCategory getAuthUserStockCategoryByCategoryName(String categoryName) {

        List<StockCategory> categoryFoundList = stockCategoryRepository
                .findAllByCategoryName(categoryName);

        if (null == categoryFoundList || categoryFoundList.isEmpty()) throw new InventoryAPIResourceNotFoundException
                ("Stock category not found", "Stock category with category name: " + categoryName + " was not found", null);

        List<String> sellerNames;
        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            sellerNames = this.sellersByAuthUserId()
                    .stream()
                    .map(Seller::getSellerEmail)
                    .collect(Collectors.toList());
            sellerNames.add(AuthenticatedUserDetails.getUserFullName());
        }
        else {
            Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());
            List<Seller> sellerList = sellerRepository.findAllByCreatedBy(seller.getCreatedBy());

            sellerNames = sellerList
                    .stream()
                    .map(Seller::getSellerEmail)
                    .collect(Collectors.toList());
            sellerNames.add(seller.getCreatedBy());
        }
        return categoryFoundList
                .stream()
                .filter(categoryFound -> categoryFound.getCategoryName().equalsIgnoreCase(categoryName)
                        && sellerNames.contains(categoryFound.getCreatedBy()))
                .collect(toSingleton());
    }

    @Override
    public List<StockCategory> getAuthUserStockCategories() {

        List<String> sellerNames;
        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            sellerNames = this.sellersByAuthUserId()
                    .stream()
                    .map(Seller::getSellerEmail)
                    .collect(Collectors.toList());
            sellerNames.add(AuthenticatedUserDetails.getUserFullName());
        }
        else {
            Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());
            List<Seller> sellerList = sellerRepository.findAllByCreatedBy(seller.getCreatedBy());

            sellerNames = sellerList
                    .stream()
                    .map(Seller::getSellerEmail)
                    .collect(Collectors.toList());
            sellerNames.add(seller.getCreatedBy());
        }

        return sellerNames
                .stream()
                .map(stockCategoryRepository::findAllByCreatedBy)
                .flatMap(List::parallelStream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Supplier> getAuthUserSuppliers() {

        List<String> sellerNames;
        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            sellerNames = this.sellersByAuthUserId()
                    .stream()
                    .map(Seller::getSellerEmail)
                    .collect(Collectors.toList());
            sellerNames.add(AuthenticatedUserDetails.getUserFullName());
        }
        else {
            Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());
            List<Seller> sellerList = sellerRepository.findAllByCreatedBy(seller.getCreatedBy());

            sellerNames = sellerList
                    .stream()
                    .map(Seller::getSellerEmail)
                    .collect(Collectors.toList());
            sellerNames.add(seller.getCreatedBy());
        }

        return sellerNames
                .stream()
                .map(supplierRepository::findAllByCreatedBy)
                .flatMap(List::parallelStream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /*
    This function reverts all income, expenses, debts, discounts and loyalties
    given to an invoice during a sale. This function is only called when a sale is reversed.
    * @return Boolean (True if successful and false otherwise).
    */
    @Override
    public Boolean revertCashFlowFromInvoice(String invoiceNumber) {

        //Fetch the income, expense, and discounts generated by that invoice number
        List<Income> incomeList = incomeRepository.findAllByIncomeReferenceContains(invoiceNumber);
        List<Expense> expenseList = expenseRepository.findAllByExpenseDescriptionContains(invoiceNumber);
        List<SalesDiscount> salesDiscountList = salesDiscountRepository.findAllByInvoiceNumber(invoiceNumber);

        if (!incomeList.isEmpty()) incomeRepository.deleteAll(incomeList);
        if (!expenseList.isEmpty()) expenseRepository.deleteAll(expenseList);
        if (!salesDiscountList.isEmpty()) salesDiscountRepository.deleteAll(salesDiscountList);

        return true;
    }

    @Override
    public Invoice processPromoIfExist(Invoice invoice) {

        //If in all the invoices none has promo applied to them, return invoice unchanged
        if(invoice.getStockSold().stream().noneMatch(StockSold::isPromoApplied))
            return invoice;

        //Get the list of promo permissible to this business
        List<Promo> promoList = promoSideService.retrieveAllPromo();

        //Stream through the stock about to be sold filter out those that flag
        // "isPromoApplied" was set to true
        invoice.getStockSold().stream()
            .filter(StockSold::isPromoApplied)
            .forEach(s -> {
                //Find the promo that matches the stock Name that has promo applied
                //Searching here operates with the notion that a stock can only belong
                //to only one promo entity for any business.
                Promo promo = promoList.stream()
                    .filter(p -> p.getPromoOnStock().stream()
                        .anyMatch(ps -> ps.getStockName().equalsIgnoreCase(s.getStockName())))
                    .collect(toSingleton());

                //Return from this function when no promo was retrieved
                //This may be an attempt to sell a stock with promo flag when this stock
                //has not been added to a promo object, hence return with an error message
                if (promo == null) throw new InventoryAPIOperationException("Sale cannot proceed",
                        "You are trying to sell this stock with promo flag when the stock has no promo assigned to it", null);

                //Get rule set for this promo and then apply the rule
                int quantitySold = s.getQuantitySold();
                int minimumPurchaseBeforeBonus = promo.getMinimumPurchase();
                if (quantitySold < minimumPurchaseBeforeBonus) return;

                int promoBonus = (quantitySold / minimumPurchaseBeforeBonus) * promo.getRewardPerMinimum();
                BigDecimal promoWorth = BigDecimal.valueOf((long) promoBonus).multiply(s.getPricePerStockSold());

                s.setSoldOnPromo(true);
                s.setQuantitySold(quantitySold + promoBonus);
                s.setQuantitySoldOnPromo(promoBonus);
                if (invoice.getDiscount() == null) invoice.setDiscount(promoWorth);
                else invoice.setDiscount(invoice.getDiscount().add(promoWorth));
                if (invoice.getReasonForDiscount() == null || invoice.getReasonForDiscount().isEmpty())
                    invoice.setReasonForDiscount("Promo was given to this invoice");
                invoice.setInvoiceTotalAmount(invoice.getInvoiceTotalAmount().add(promoWorth));

            });

        return invoice;
    }
}
