package com.chrisworks.personal.inventorysystem.Backend.Services;


import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;

import java.util.List;


/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
public interface GenericService {

    Customer addCustomer(Customer customer);

    Supplier addSupplier(Supplier supplier);

    StockCategory addStockCategory(StockCategory stockCategory);

    Expense addExpense(Expense expense);

    Income addIncome(Income income);

    Shop shopBySellerName(String sellerName);

    Warehouse warehouseByWarehouseAttendantName(String warehouseAttendantName);

    List<Warehouse> warehouseByAuthUserId();

    List<Shop> shopByAuthUserId();

    List<Seller> sellersByAuthUserId();

    Customer getAuthUserCustomerByPhoneNumber(String customerPhoneNumber);

    Supplier getAuthUserSupplierByPhoneNumber(String supplierPhoneNumber);

    StockCategory getAuthUserStockCategoryByCategoryName(String categoryName);

    List<StockCategory> getAuthUserStockCategories();

    List<Supplier> getAuthUserSuppliers();

    Boolean revertCashFlowFromInvoice(String invoiceNumber);

    Invoice processPromoIfExist(Invoice invoice);
}
