package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDataValidationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDuplicateEntryException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.*;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
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

    private SellerRepository sellerRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public BusinessOwnerServicesImpl(BusinessOwnerRepository businessOwnerRepository, ReturnedStockRepository returnedStockRepository,
                                     ExpenseRepository expenseRepository, IncomeRepository incomeRepository,
                                     BCryptPasswordEncoder passwordEncoder, SellerRepository sellerRepository) {

        this.businessOwnerRepository = businessOwnerRepository;
        this.returnedStockRepository = returnedStockRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.passwordEncoder = passwordEncoder;
        this.sellerRepository = sellerRepository;
    }

    @Override
    public BusinessOwner createAccount(BusinessOwner businessOwner) {

        if (businessOwnerRepository.findDistinctByBusinessOwnerEmail(businessOwner.getBusinessOwnerEmail()) != null) throw new
                InventoryAPIDuplicateEntryException("Email already exist", "A business account already exist with the email address: " +
                businessOwner.getBusinessOwnerEmail(), null);

        if (sellerRepository.findDistinctBySellerEmail(businessOwner.getBusinessOwnerEmail()) != null) throw new
                InventoryAPIDuplicateEntryException("Email already exist", "A seller account already exist with the email address: " +
                businessOwner.getBusinessOwnerEmail(), null);

        businessOwner.setBusinessOwnerPassword
                (passwordEncoder.encode(businessOwner.getBusinessOwnerPassword()));

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

        if (null == businessOwnerId || businessOwnerId < 0 || !businessOwnerId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("business owner id error", "business owner id is empty or not a valid number", null);

        if (!businessOwnerId.equals(AuthenticatedUserDetails.getUserId())) throw new InventoryAPIOperationException
                ("business owner id error", "Authenticated user id does not match id from request", null);

        if (null == businessOwnerUpdates) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find business owner entity to save", null);

        AtomicReference<BusinessOwner> updatedDetails = new AtomicReference<>();

        Optional<BusinessOwner> optionalBusinessOwner = businessOwnerRepository.findById(businessOwnerId);

        if (!optionalBusinessOwner.isPresent()) throw new InventoryAPIResourceNotFoundException
                ("Entity to update not found", "No business owner exist with id: " + businessOwnerId, null);

        optionalBusinessOwner.ifPresent(businessOwner -> {

            businessOwner.setBusinessOwnerFullName(businessOwnerUpdates.getBusinessOwnerFullName());
            businessOwner.setBusinessOwnerPhoneNumber(businessOwnerUpdates.getBusinessOwnerPhoneNumber());
            businessOwner.setBusinessName(businessOwnerUpdates.getBusinessName());
            businessOwner.setUpdateDate(new Date());
            updatedDetails.set(businessOwnerRepository.save(businessOwner));
        });

        return updatedDetails.get();
    }
}
