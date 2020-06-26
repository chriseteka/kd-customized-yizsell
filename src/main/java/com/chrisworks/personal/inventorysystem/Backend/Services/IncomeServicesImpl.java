package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Income;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.IncomeRepository;
import com.chrisworks.personal.inventorysystem.Backend.Services.CacheManager.Interfaces.CacheInterface;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.getGSon;
import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.isDateEqual;

/**
 * @author Chris_Eteka
 * @since 11/28/2019
 * @email chriseteka@gmail.com
 */
@Service
public class IncomeServicesImpl implements IncomeServices {

    private final IncomeRepository incomeRepository;

    private final GenericService genericService;

    private final CacheInterface<com.chrisworks.personal.inventorysystem.Backend.Entities.DTO.Income> incomeCacheManager;
    private final String REDIS_TABLE_KEY = "INCOME";

    @Autowired
    public IncomeServicesImpl(IncomeRepository incomeRepository, GenericService genericService,
                              CacheInterface<com.chrisworks.personal.inventorysystem.Backend.Entities.DTO.Income> incomeCacheManager) {
        this.incomeRepository = incomeRepository;
        this.genericService = genericService;
        this.incomeCacheManager = incomeCacheManager;
    }

    @Override
    public Income createEntity(Income income) {

        if (null == income) throw new InventoryAPIOperationException("Income to save doesnt exist",
                "Income entity to save was not found, review your inputs and try again", null);

        Income savedIncome = genericService.addIncome(income);
        if (incomeCacheManager.nonEmpty(REDIS_TABLE_KEY)) cacheIncome(income);

        return savedIncome;
    }

    @Override
    public Income updateEntity(Long id, Income income) {

        return incomeRepository.findById(id).map(incomeFound -> {

            if (!incomeFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Operation not allowed",
                        "You cannot update an income not created by you", null);

            incomeFound.setUpdateDate(new Date());
            incomeFound.setIncomeAmount(income.getIncomeAmount());
            incomeFound.setIncomeReference(income.getIncomeReference());
            incomeFound.setIncomeTypeVal(income.getIncomeTypeVal());

            Income updatedIncome = incomeRepository.save(incomeFound);

            incomeCacheManager.updateCacheDetail(REDIS_TABLE_KEY, updatedIncome.toDTO(), id);

            return updatedIncome;
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

        if (incomeCacheManager.nonEmpty(REDIS_TABLE_KEY)) return fetchIncomeFromCache();

        List<Income> incomeList;

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)
                || AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            incomeList =  incomeRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());
        else {
            incomeList = genericService.sellersByAuthUserId()
                    .stream()
                    .map(Seller::getSellerEmail)
                    .map(incomeRepository::findAllByCreatedBy)
                    .flatMap(List::parallelStream)
                    .collect(Collectors.toList());
            incomeList.addAll(incomeRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName()));
        }

        if (!incomeList.isEmpty()) incomeList.forEach(this::cacheIncome);

        return incomeList;
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
                incomeCacheManager.removeDetail(REDIS_TABLE_KEY, incomeId);
                return income;
            }

            boolean match = genericService.sellersByAuthUserId()
                    .stream()
                    .map(Seller::getSellerEmail)
                    .anyMatch(sellerName -> sellerName.equalsIgnoreCase(income.getCreatedBy()));

            if (match) {
                incomeRepository.delete(income);
                incomeCacheManager.removeDetail(REDIS_TABLE_KEY, incomeId);
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
                .filter(income -> isDateEqual(income.getCreatedDate(), createdOn))
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
    public List<Income> approveIncome(Long... incomeId) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        List<Income> editedIncomeList = fetchAllUnApprovedIncome()
                .stream()
                .filter(income -> Arrays.asList(incomeId).contains(income.getIncomeId()))
                .peek(incomeFound -> {

                    incomeFound.setIncomeTypeVal(String.valueOf(incomeFound.getIncomeTypeValue()));
                    incomeFound.setApprovedBy(AuthenticatedUserDetails.getUserFullName());
                    incomeFound.setApproved(true);
                    incomeFound.setApprovedDate(new Date());
                }).collect(Collectors.toList());

        List<Income> approvedIncomeList = incomeRepository.saveAll(editedIncomeList);
        approvedIncomeList.forEach(income -> incomeCacheManager
                .updateCacheDetail(REDIS_TABLE_KEY, income.toDTO(), income.getIncomeId()));

        return approvedIncomeList;
    }

    @Override
    public List<Income> fetchAllUnApprovedIncome() {

        return getEntityList()
                .stream()
                .filter(income -> !income.getApproved())
                .collect(Collectors.toList());
    }

    @Override
    public List<Income> fetchAllByDescriptionContains(String description) {

        return getEntityList()
                .stream()
                .filter(income -> income.getIncomeReference().contains(description))
                .collect(Collectors.toList());
    }

    private void cacheIncome(Income income){
        incomeCacheManager.cacheDetail(REDIS_TABLE_KEY, income.toDTO(), income.getIncomeId());
    }

    private List<Income> fetchIncomeFromCache(){

        return incomeCacheManager.fetchDetailsByKey(REDIS_TABLE_KEY, data ->
                new ArrayList<>(getGSon().fromJson(data.stream()
                        .map(entry -> getGSon().toJson(entry.getValue()))
                        .collect(Collectors.toList()).toString(),
                    new TypeToken<ArrayList<com.chrisworks.personal.inventorysystem.Backend.Entities.DTO.Income>>(){}.getType())))
            .stream().map(com.chrisworks.personal.inventorysystem.Backend.Entities.DTO.Income::fromDTO)
            .collect(Collectors.toList());

    }
}
