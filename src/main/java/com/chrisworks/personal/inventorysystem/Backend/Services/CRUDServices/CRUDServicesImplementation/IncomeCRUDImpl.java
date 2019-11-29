package com.chrisworks.personal.inventorysystem.Backend.Services.CRUDServices.CRUDServicesImplementation;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Income;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.IncomeRepository;
import com.chrisworks.personal.inventorysystem.Backend.Services.CRUDServices.CRUDServices;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Chris_Eteka
 * @since 11/28/2019
 * @email chriseteka@gmail.com
 */
@Service
public class IncomeCRUDImpl implements CRUDServices<Income> {

    private final IncomeRepository incomeRepository;

    @Autowired
    public IncomeCRUDImpl(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    @Override
    public Income createEntity(Income income) {

        income.setCreatedBy(AuthenticatedUserDetails.getUserFullName());

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            income.setApproved(true);
            income.setApprovedDate(new Date());
            income.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
        }

        return incomeRepository.save(income);
    }

    @Override
    public Income updateEntity(Long id, Income income) {

        AtomicReference<Income> updatedIncome = new AtomicReference<>();

        incomeRepository.findById(id).ifPresent(incomeFound -> {

            incomeFound.setUpdateDate(new Date());
            incomeFound.setIncomeAmount(income.getIncomeAmount() != null ? income.getIncomeAmount()
                    : incomeFound.getIncomeAmount());
            incomeFound.setIncomeReference(income.getIncomeReference() != null ? income.getIncomeReference()
                    : incomeFound.getIncomeReference());
            incomeFound.setIncomeType(income.getIncomeType() != null ? income.getIncomeType()
                    : incomeFound.getIncomeType());

            updatedIncome.set(incomeRepository.save(incomeFound));
        });

        return updatedIncome.get();
    }

    @Override
    public Income getSingleEntity(Long entityId) {

        AtomicReference<Income> income = new AtomicReference<>();

        incomeRepository.findById(entityId).ifPresent(income::set);

        return income.get();
    }

    @Override
    public List<Income> getEntityList() {

        return incomeRepository.findAll();
    }

    @Override
    public Income deleteEntity(Long incomeId) {

        AtomicReference<Income> incomeToDelete = new AtomicReference<>();

        incomeRepository.findById(incomeId).ifPresent(income -> {

            incomeToDelete.set(income);
            incomeRepository.delete(income);
        });

        return incomeToDelete.get();
    }
}
