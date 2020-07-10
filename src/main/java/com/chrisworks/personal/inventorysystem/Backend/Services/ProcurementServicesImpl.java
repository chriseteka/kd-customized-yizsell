package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDuplicateEntryException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ProcuredStockRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ProcurementRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SupplierRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;
import static ir.cafebabe.math.utils.BigDecimalUtils.is;

/**
 * @author Chris_Eteka
 * @since 6/1/2020
 * @email chriseteka@gmail.com
 */
@Service
@RequiredArgsConstructor
public class ProcurementServicesImpl implements ProcurementServices {

    private final ProcurementRepository procurementRepository;
    private final ProcuredStockRepository procuredStockRepository;
    private final ExpenseServices expenseServices;
    private final SupplierRepository supplierRepository;
    private final WarehouseStockServices warehouseStockServices;

    @Override
    @Transactional
    public Procurement createEntity(Procurement procurement) {

        preAuthorize();

        String waybillId = procurement.getWaybillId();
        BigDecimal procurementAmount = procurement.getAmount();
        Supplier supplier = procurement.getSupplier();

        if (!waybillId.isEmpty()) {

            if (procurementRepository.findDistinctByWaybillIdAndCreatedBy
                    (waybillId, AuthenticatedUserDetails.getUserFullName()).isPresent())
                throw new InventoryAPIDuplicateEntryException("Duplicate Entry",
                        "Procurement already exist with the id: " + waybillId + " in your business", null);

            if (procurement.isRecordAsExpense() && is(procurementAmount).isPositive()) {
                Expense expense = new Expense(100, procurementAmount,
                        "Procurement made with waybillId: " + waybillId);
                expenseServices.createEntity(expense);
            }
        }

        procurement.setSupplier(supplierRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName())
                .stream().filter(s -> s.getSupplierEmail().equalsIgnoreCase(supplier.getSupplierEmail())
                        && s.getSupplierPhoneNumber().equalsIgnoreCase(supplier.getSupplierPhoneNumber()))
                .collect(toSingleton()));

        return procurementRepository.save(procurement);
    }

    @Override
    @Transactional
    public Procurement updateEntity(Long entityId, Procurement procurement) {

        return Optional.of(getSingleEntity(entityId))
            .map(existingProcurement -> {

                if (procurement.procurementAmountInAccurate())
                    throw new InventoryAPIOperationException("Total Procurement Amount Mismatch",
                            "Total procurement amount does not tally with the sum of the individual stock total amount", null);

                if (procurement.equals(existingProcurement)) throw new InventoryAPIOperationException("Cannot Update",
                        "You cannot update this procurement since there are no changes to the old values", null);

                String waybillId = procurement.getWaybillId();
                BigDecimal procurementAmount = procurement.getAmount();

                if (!waybillId.isEmpty()){

                    Optional<Procurement> optionalProcurement = procurementRepository
                            .findDistinctByWaybillIdAndCreatedBy(waybillId, AuthenticatedUserDetails.getUserFullName());

                    if (optionalProcurement.isPresent() && !optionalProcurement.get().getProcurementId().equals(entityId))
                        throw new InventoryAPIOperationException("Operation not allowed", "You are trying to update a " +
                                "procurement with a waybillId that is attached to another procurement", null);

                    if (!existingProcurement.isRecordAsExpense() && procurement.isRecordAsExpense()
                            && is(procurementAmount).isPositive()){

                        Expense expense = new Expense(100, procurementAmount,
                                "Procurement made with waybillId: " + waybillId);
                        expenseServices.createEntity(expense);
                        existingProcurement.setRecordAsExpense(procurement.isRecordAsExpense());
                    }
                    else if (existingProcurement.isRecordAsExpense()) {

                        Expense existingExpense = expenseServices.fetchExpensesByDescription
                                (existingProcurement.getWaybillId()).stream().collect(toSingleton());

                        if (!procurement.isRecordAsExpense()){

                            if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
                                throw new InventoryAPIOperationException("Operation not allowed", "You cannot edit this " +
                                        "procurement, since you are trying to remove expense attached to it, only admin can do this", null);

                            existingProcurement.setRecordAsExpense(procurement.isRecordAsExpense());
                            expenseServices.deleteEntity(existingExpense.getExpenseId());
                        }
                        else if (is(procurementAmount).isPositive() && is(procurementAmount).notEq(existingProcurement.getAmount())) {

                            existingExpense.setExpenseTypeVal(String.valueOf(existingExpense.getExpenseTypeValue()));
                            existingExpense.setExpenseDescription("Procurement made with waybillId: " + waybillId);
                            existingExpense.setExpenseAmount(procurementAmount);
                            expenseServices.updateEntity(existingExpense.getExpenseId(), existingExpense);
                        }
                        if (!existingProcurement.getWaybillId().equalsIgnoreCase(waybillId)
                                && procurement.isRecordAsExpense()){

                            existingExpense.setExpenseTypeVal(String.valueOf(existingExpense.getExpenseTypeValue()));
                            existingExpense.setExpenseDescription("Procurement made with waybillId: " + waybillId);
                            expenseServices.updateEntity(existingExpense.getExpenseId(), existingExpense);
                        }
                    }

                    existingProcurement.setWaybillId(waybillId);
                }

                Set<ProcuredStock> incomingStocks = procurement.getStocks();
                if (!incomingStocks.isEmpty()) {
                    Set<ProcuredStock> existingStocks = existingProcurement.getStocks();
                    if (!existingStocks.isEmpty()) {
                        existingProcurement.setStocks(null);
                        procuredStockRepository.deleteAll(existingStocks);
                    }
                    existingProcurement.setStocks(incomingStocks);
                }

                existingProcurement.setUpdateDate(new Date());
                existingProcurement.setAmount(procurementAmount);
                return procurementRepository.save(existingProcurement);
            })
            .orElseThrow(() -> new InventoryAPIOperationException("Could not update",
                    "Could not update procurement with id: " + entityId, null));
    }

    @Override
    public Procurement getSingleEntity(Long entityId) {

        preAuthorize();

        return procurementRepository.findById(entityId)
            .map(procurement -> {

                if (!procurement.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                    throw new InventoryAPIOperationException("Procurement not yours", "You are trying to" +
                            " retrieve a procurement that is not yours, review your inputs and try again", null);

                return procurement;
            })
            .orElseThrow(() -> new InventoryAPIResourceNotFoundException("Procurement not found",
                    "Procurement with id: " + entityId + " was not found", null));
    }

    @Override
    public List<Procurement> getEntityList() {

        preAuthorize();

        return procurementRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());
    }

    @Override
    @Transactional
    public Procurement deleteEntity(Long entityId) {

        Procurement procurement = getSingleEntity(entityId);
        Expense expense = expenseServices.fetchExpensesByDescription(procurement.getWaybillId())
                .stream().collect(toSingleton());
        expenseServices.deleteEntity(expense.getExpenseId());

        procurementRepository.delete(procurement);

        return procurement;
    }

    private void preAuthorize(){

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);
    }

    @Override
    public Procurement fetchProcurementByWaybillId(String waybillId) {

        preAuthorize();

        return procurementRepository
            .findDistinctByWaybillIdAndCreatedBy(waybillId, AuthenticatedUserDetails.getUserFullName())
            .orElseThrow(() -> new InventoryAPIResourceNotFoundException("Procurement not found",
                    "Procurement with waybillId: " + waybillId + " was not found.", null));
    }

    @Override
    @Transactional
    public Procurement moveProcurementToWarehouse(Long warehouseId, Long procurementId) {

        Procurement procurement = getSingleEntity(procurementId);
        procurement.verifyNotMovedYet();

        Map<StockCategory, List<WarehouseStocks>> categoryListMap = warehouseStockServices.allStockByWarehouseId
                (warehouseId).stream().collect(Collectors.groupingBy(WarehouseStocks::getStockCategory));
        Supplier supplier = procurement.getSupplier();

        List<WarehouseStocks> newWarehouseStockList = procurement.getStocks().stream().map(stock -> {

            String stockName = stock.getStockName();
            WarehouseStocks newWarehouseStock = new WarehouseStocks();
            newWarehouseStock.setStockName(stockName);
            newWarehouseStock.setStockCategory(categoryListMap.entrySet()
                    .stream().filter(entry -> entry.getValue().stream()
                            .filter(ws -> ws.getStockName().contains(stockName)).limit(1).collect(toSingleton()) != null)
                    .limit(1).collect(toSingleton()).getKey());
            newWarehouseStock.setPricePerStockPurchased(stock.getPricePerStockPurchased());
            newWarehouseStock.setLastRestockQuantity(stock.getQuantity());
            newWarehouseStock.setStockQuantityPurchased(stock.getQuantity());
            newWarehouseStock.setStockPurchasedTotalPrice(stock.getStockTotalPrice());
            newWarehouseStock.setLastRestockPurchasedFrom(supplier);

            return newWarehouseStock;
        }).collect(Collectors.toList());

        if (!warehouseStockServices.createStockListInWarehouse(warehouseId, newWarehouseStockList)
                .getFailedUploads().isEmpty()) throw new InventoryAPIOperationException("Cannot save procurements",
                "Procurements could not be saved to the warehouse specified", null);

        procurement.setMovedToWarehouse(true);
        procurement.setMovedToWarehouseId(warehouseId);
        return procurementRepository.save(procurement);
    }
}
