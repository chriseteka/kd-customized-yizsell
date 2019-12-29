package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Income;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.IncomeRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;

/**
 * @author Chris_Eteka
 * @since 11/28/2019
 * @email chriseteka@gmail.com
 */
@Service
public class IncomeServicesImpl implements IncomeServices {

    private final IncomeRepository incomeRepository;

    private final GenericService genericService;

    @Autowired
    public IncomeServicesImpl(IncomeRepository incomeRepository, GenericService genericService) {
        this.incomeRepository = incomeRepository;
        this.genericService = genericService;
    }

    @Override
    public Income createEntity(Income income) {

        if (null == income) throw new InventoryAPIOperationException("Income to save doesnt exist",
                "Income entity to save was not found, review your inputs and try again", null);

        return genericService.addIncome(income);
    }

    @Override
    public Income updateEntity(Long id, Income income) {

        return incomeRepository.findById(id).map(incomeFound -> {

            if (!incomeFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Operation not allowed",
                        "You cannot update an income not created by you", null);

            incomeFound.setUpdateDate(new Date());
            incomeFound.setIncomeAmount(income.getIncomeAmount() != null ? income.getIncomeAmount()
                    : incomeFound.getIncomeAmount());
            incomeFound.setIncomeReference(income.getIncomeReference() != null ? income.getIncomeReference()
                    : incomeFound.getIncomeReference());
            incomeFound.setIncomeType(income.getIncomeType() != null ? income.getIncomeType()
                    : incomeFound.getIncomeType());

            return incomeRepository.save(incomeFound);
        }).orElse(null);
    }

    @Override
    public Income getSingleEntity(Long incomeId) {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

            return incomeRepository.findById(incomeId)
                    .map(income -> {

                        if (income.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                            return income;

                        boolean match = genericService.sellersByAuthUserId()
                                .stream()
                                .map(Seller::getSellerEmail)
                                .anyMatch(sellerName -> sellerName.equalsIgnoreCase(income.getCreatedBy()));

                        if (match) return income;
                        else throw new InventoryAPIOperationException("Operation not allowed",
                                "You cannot view an income not created by you, or any of your sellers", null);
                    }).orElse(null);
        }

        return incomeRepository.findById(incomeId)
                .map(income -> {

                    if (!income.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                        throw new InventoryAPIOperationException("Operation not allowed",
                                "You cannot view an income not created by you", null);

                    return income;
                }).orElse(null);
    }

    @Override
    public List<Income> getEntityList() {

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)
                || AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            return incomeRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());

        Set<Income> incomeSet = genericService.sellersByAuthUserId()
                .stream()
                .map(Seller::getSellerEmail)
                .map(incomeRepository::findAllByCreatedBy)
                .flatMap(List::parallelStream)
                .collect(Collectors.toSet());
        incomeSet.addAll(incomeRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName()));

        return new ArrayList<>(incomeSet);
    }

    @Override
    public Income deleteEntity(Long incomeId) {

        if (AuthenticatedUserDetails.getAccount_type() == null ||
                !AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return incomeRepository.findById(incomeId).map(income -> {

            if (income.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())){
                incomeRepository.delete(income);
                return income;
            }

            boolean match = genericService.sellersByAuthUserId()
                    .stream()
                    .map(Seller::getSellerEmail)
                    .anyMatch(sellerName -> sellerName.equalsIgnoreCase(income.getCreatedBy()));

            if (match) {
                incomeRepository.delete(income);
                return income;
            }
            else throw new InventoryAPIOperationException("Operation not allowed",
                    "You cannot delete an income not created by you or any of your sellers", null);
        }).orElse(null);
    }

    @Override
    public List<Income> fetchAllApprovedIncome() {

        return getEntityList()
                .stream()
                .filter(Income::getApproved)
                .collect(Collectors.toList());
    }

    @Override
    public List<Income> fetchAllUnApprovedIncomeByCreator(String createdBy) {

        return fetchAllUnApprovedIncome()
                .stream()
                .filter(income -> income.getCreatedBy().equalsIgnoreCase(createdBy))
                .collect(Collectors.toList());
    }

    @Override
    public List<Income> fetchIncomeCreatedBy(String createdBy) {

        return getEntityList()
                .stream()
                .filter(income -> income.getCreatedBy().equalsIgnoreCase(createdBy))
                .collect(Collectors.toList());
    }

    @Override
    public List<Income> fetchAllIncomeCreatedOn(Date createdOn) {

        return getEntityList()
                .stream()
                .filter(income -> income.getCreatedDate().compareTo(createdOn) == 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<Income> fetchAllIncomeBetween(Date from, Date to) {

        return getEntityList()
                .stream()
                .filter(income -> (income.getCreatedDate().compareTo(from) >= 0)
                        && (to.compareTo(income.getCreatedDate()) >= 0))
                .collect(Collectors.toList());
    }

    @Override
    public List<Income> fetchAllIncomeByType(int incomeTypeValue) {

        return getEntityList()
                .stream()
                .filter(income -> income.getIncomeTypeValue() == incomeTypeValue)
                .collect(Collectors.toList());
    }

    @Override
    public List<Income> fetchAllIncomeInShop(Long shopId) {

        if (AuthenticatedUserDetails.getAccount_type() == null
                || AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return genericService.shopByAuthUserId()
                .stream()
                .filter(shop -> shop.getShopId().equals(shopId))
                .map(incomeRepository::findAllByShop)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList());
    }

    @Override
    public Income approveIncome(Long incomeId) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        Income incomeFound = fetchAllUnApprovedIncome()
                .stream()
                .filter(income -> income.getIncomeId().equals(incomeId))
                .collect(toSingleton());

        if (incomeFound == null) throw new InventoryAPIResourceNotFoundException
                ("Income not found", "Income with id: " + incomeId + " was not found in your list of unapproved income", null);

        incomeFound.setIncomeTypeVal(String.valueOf(incomeFound.getIncomeTypeValue()));
        incomeFound.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
        incomeFound.setApproved(true);
        incomeFound.setApprovedDate(new Date());

        return incomeRepository.save(incomeFound);
    }

    @Override
    public List<Income> fetchAllUnApprovedIncome() {

        return getEntityList()
                .stream()
                .filter(income -> !income.getApproved())
                .collect(Collectors.toList());
    }
}
