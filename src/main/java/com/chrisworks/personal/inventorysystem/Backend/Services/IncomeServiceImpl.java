package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Income;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.IncomeRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ShopRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Chris_Eteka
 * @since 11/28/2019
 * @email chriseteka@gmail.com
 */
@Service
public class IncomeServiceImpl implements IncomeServices {

    private final IncomeRepository incomeRepository;

    private final ShopRepository shopRepository;

    @Autowired
    public IncomeServiceImpl(IncomeRepository incomeRepository, ShopRepository shopRepository) {
        this.incomeRepository = incomeRepository;
        this.shopRepository = shopRepository;
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

    @Override
    public List<Income> fetchAllApprovedIncome() {

        return incomeRepository.findAllByApprovedTrue();
    }

    @Override
    public List<Income> fetchAllUnApprovedIncomeByCreator(String createdBy) {

        return incomeRepository.findAllByCreatedByAndApprovedIsFalse(createdBy);
    }

    @Override
    public List<Income> fetchIncomeCreatedBy(String createdBy) {

        return incomeRepository.findAllByCreatedBy(createdBy);
    }

    @Override
    public List<Income> fetchAllIncomeCreatedOn(Date createdOn) {

        return incomeRepository.findAllByCreatedDate(createdOn);
    }

    @Override
    public List<Income> fetchAlIncomeBetween(Date from, Date to) {

        return incomeRepository.findAllByCreatedDateIsBetween(from, to);
    }

    @Override
    public List<Income> fetchAllIncomeByType(int incomeTypeValue) {

        return incomeRepository.findAllByIncomeTypeValue(incomeTypeValue);
    }

    @Override
    public List<Income> fetchAllIncomeInShop(Long shopId) {

        return shopRepository
                .findById(shopId)
                .map(shop -> new ArrayList<>(shop.getIncome()))
                .orElse(null);
    }
}
