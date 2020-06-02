package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Expense;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ProcuredStock;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Procurement;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDuplicateEntryException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ProcuredStockRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ProcurementRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    @Override
    @Transactional
    public Procurement createEntity(Procurement procurement) {

        preAuthorize();

        if (procurementRepository.findDistinctByWaybillIdAndCreatedBy
                (procurement.getWaybillId(), AuthenticatedUserDetails.getUserFullName()).isPresent())
            throw new InventoryAPIDuplicateEntryException("Duplicate Entry",
                    "Procurement already exist with the id: " + procurement.getWaybillId() + " in your business", null);

        if (procurement.isRecordAsExpense()) {
            Expense expense = new Expense(100, procurement.getAmount(),
                    "Procurement made with waybillId: " + procurement.getWaybillId());
            expenseServices.createEntity(expense);
        }

        return procurementRepository.save(procurement);
    }

    @Override
    @Transactional
    public Procurement updateEntity(Long entityId, Procurement procurement) {

        return Optional.of(getSingleEntity(entityId))
            .map(existingProcurement -> {

                if (procurement.procurementAmountInAccurate()){
                    throw new InventoryAPIOperationException("Total Procurement Amount Mismatch",
                            "Total procurement amount does not tally with the sum of the individual stock total amount", null);
                }

                if (procurement.equals(existingProcurement)) throw new InventoryAPIOperationException("Cannot Update",
                        "You cannot update this procurement since there are no changes to the old values", null);

                existingProcurement.setUpdateDate(new Date());
                Set<ProcuredStock> incomingStocks = procurement.getStocks();
                if (!incomingStocks.isEmpty()) {
                    Set<ProcuredStock> existingStocks = existingProcurement.getStocks();
                    existingProcurement.setStocks(null);
                    procuredStockRepository.deleteAll(existingStocks);
                    existingProcurement.setStocks(incomingStocks);
                }

                if (existingProcurement.isRecordAsExpense()
                        && is(procurement.getAmount()).notEq(existingProcurement.getAmount())){

                    Expense existingExpense = expenseServices.fetchExpensesByDescription(procurement.getWaybillId())
                            .stream().collect(toSingleton());

                    existingExpense.setExpenseTypeVal(String.valueOf(existingExpense.getExpenseTypeValue()));
                    existingExpense.setExpenseDescription("Procurement made with waybillId: " + procurement.getWaybillId());
                    existingExpense.setExpenseAmount(procurement.getAmount());
                    expenseServices.updateEntity(existingExpense.getExpenseId(), existingExpense);
                }

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
    public Procurement deleteEntity(Long entityId) {

        Procurement procurement = getSingleEntity(entityId);

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
}