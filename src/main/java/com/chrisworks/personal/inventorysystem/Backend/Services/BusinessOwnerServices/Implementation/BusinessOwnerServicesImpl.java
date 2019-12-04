package com.chrisworks.personal.inventorysystem.Backend.Services.BusinessOwnerServices.Implementation;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.*;
import com.chrisworks.personal.inventorysystem.Backend.Services.BusinessOwnerServices.BusinessOwnerServices;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
@Service
public class BusinessOwnerServicesImpl implements BusinessOwnerServices {

    private BusinessOwnerRepository businessOwnerRepository;

    private ReturnedStockRepository returnedStockRepository;

    private ExpenseRepository expenseRepository;

    private IncomeRepository incomeRepository;

    @Autowired
    public BusinessOwnerServicesImpl(BusinessOwnerRepository businessOwnerRepository, ReturnedStockRepository returnedStockRepository,
                                     ExpenseRepository expenseRepository, IncomeRepository incomeRepository) {

        this.businessOwnerRepository = businessOwnerRepository;
        this.returnedStockRepository = returnedStockRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
    }

    @Override
    public BusinessOwner createAccount(BusinessOwner businessOwner) {

        return businessOwnerRepository.save(businessOwner);
    }

    @Override
    public Boolean approveIncome(Long incomeId) {

        AtomicReference<Boolean> incomeApprovedFlag = new AtomicReference<>();

        incomeRepository.findById(incomeId).ifPresent(income -> {

            income.setApproved(true);
            income.setUpdateDate(new Date());
            income.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
            incomeApprovedFlag.set(incomeRepository.save(income) != null);
        });

        return incomeApprovedFlag.get();
    }

    @Override
    public Boolean approveExpense(Long expenseId) {

        AtomicReference<Boolean> expenseApprovedFlag = new AtomicReference<>();

        expenseRepository.findById(expenseId).ifPresent(expense -> {

            expense.setUpdateDate(new Date());
            expense.setApproved(true);
            expense.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
            expenseApprovedFlag.set(expenseRepository.save(expense) != null);
        });

        return expenseApprovedFlag.get();
    }

    @Override
    public Boolean approveReturn(Long returnedStockId) {

        AtomicReference<Boolean> returnApprovedFlag = new AtomicReference<>();

        returnedStockRepository.findById(returnedStockId).ifPresent(returnedStock -> {

            returnedStock.setUpdateDate(new Date());
            returnedStock.setApproved(true);
            returnedStock.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
            returnApprovedFlag.set(returnedStockRepository.save(returnedStock) != null);
        });

        return returnApprovedFlag.get();
    }

    @Override
    public BusinessOwner updateAccount(Long businessOwnerId, BusinessOwner businessOwnerUpdates) {

        AtomicReference<BusinessOwner> updatedDetails = new AtomicReference<>();

        businessOwnerRepository.findById(businessOwnerId).ifPresent(businessOwner -> {

            businessOwner.setBusinessOwnerFullName(businessOwnerUpdates.getBusinessOwnerFullName());
            businessOwner.setBusinessOwnerPhoneNumber(businessOwnerUpdates.getBusinessOwnerPhoneNumber());
            businessOwner.setBusinessName(businessOwnerUpdates.getBusinessName());
            businessOwner.setUpdateDate(new Date());
            updatedDetails.set(businessOwnerRepository.save(businessOwner));
        });

        return updatedDetails.get();
    }

    @Override
    public BusinessOwner fetchBusinessOwner(Long id) {

        AtomicReference<BusinessOwner> businessOwnerRetrieved = new AtomicReference<>();

        businessOwnerRepository.findById(id).ifPresent(businessOwnerRetrieved::set);

        return businessOwnerRetrieved.get();
    }
}
