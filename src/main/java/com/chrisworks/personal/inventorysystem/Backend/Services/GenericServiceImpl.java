package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDuplicateEntryException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.*;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Chris_Eteka
 * @since 11/27/2019
 * @email chriseteka@gmail.com
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

    @Autowired
    public GenericServiceImpl
            (SupplierRepository supplierRepository, CustomerRepository customerRepository,
             ExpenseRepository expenseRepository, IncomeRepository incomeRepository,
             SellerRepository sellerRepository, WarehouseRepository warehouseRepository,
             StockCategoryRepository stockCategoryRepository, ShopRepository shopRepository)
    {
        this.supplierRepository = supplierRepository;
        this.customerRepository = customerRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.sellerRepository = sellerRepository;
        this.warehouseRepository = warehouseRepository;
        this.stockCategoryRepository = stockCategoryRepository;
        this.shopRepository = shopRepository;
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

        if(AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER) &&
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

        if(!sellerFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
            throw new InventoryAPIOperationException("Not allowed",
                    "You cannot view details of a warehouse attendant not created by you", null);

        if (null == sellerFound.getWarehouse()) throw new InventoryAPIResourceNotFoundException
                ("Seller does not have a warehouse", "Seller has not been assigned to any warehouse", null);

        return sellerFound.getWarehouse();
    }

    @Override
    public List<Warehouse> warehouseByAuthUserId() {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        Long authUserId = AuthenticatedUserDetails.getUserId();

        ACCOUNT_TYPE authUserType = AuthenticatedUserDetails.getAccount_type();

        String authUserMail = AuthenticatedUserDetails.getUserFullName();

        if (null == authUserId || authUserId < 0 || !authUserId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("authUserId id error", "Authenticated user id is empty or not a valid number", null);

        if (authUserType.equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            return warehouseRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());
        if (authUserType.equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT)){

            Seller sellerFound = sellerRepository.findDistinctBySellerFullNameOrSellerEmail(authUserMail, authUserMail);
            return new ArrayList<>(Collections.singleton(sellerFound.getWarehouse()));
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
        if (authUserType.equals(ACCOUNT_TYPE.SHOP_SELLER)){

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
}
