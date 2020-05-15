package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockCategory;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.StockCategoryRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;

/**
 * @author Chris_Eteka
 * @since 12/5/2019
 * @email chriseteka@gmail.com
 */
@Service
public class StockCategoryServicesImpl implements StockCategoryServices {

    private final StockCategoryRepository stockCategoryRepository;

    private final GenericService genericService;

    private final SellerRepository sellerRepository;

    @Autowired
    public StockCategoryServicesImpl(StockCategoryRepository stockCategoryRepository, GenericService genericService,
                                     SellerRepository sellerRepository) {
        this.stockCategoryRepository = stockCategoryRepository;
        this.genericService = genericService;
        this.sellerRepository = sellerRepository;
    }

    @Override
    public StockCategory createEntity(StockCategory stockCategory) {

        boolean match = getEntityList()
                .stream()
                .anyMatch(category -> category.getCategoryName().equalsIgnoreCase(stockCategory.getCategoryName()));

        if (match) throw new InventoryAPIOperationException("Stock category already exist",
                "Stock category already exist in your shop/business with the name: " + stockCategory.getCategoryName() +
                        ", hence it cannot add it to this business' list of stock categories.", null);

        return genericService.addStockCategory(stockCategory);
    }

    @Override
    public StockCategory updateEntity(Long entityId, StockCategory stockCategory) {

        return stockCategoryRepository.findById(entityId).map(stockCategoryFound -> {

            if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER) &&
                    !stockCategoryFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Operation not allowed",
                        "You cannot update a stock category not created by you", null);

            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

                boolean match = genericService.sellersByAuthUserId()
                        .stream()
                        .map(Seller::getSellerEmail)
                        .anyMatch(sellerName -> sellerName.equalsIgnoreCase(stockCategoryFound.getCreatedBy()));

                if (!match && !stockCategoryFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                    throw new InventoryAPIOperationException("Operation not allowed",
                            "You cannot update a stock category not created by you or any of your sellers.", null);
            }

            stockCategoryFound.setUpdateDate(new Date());
            stockCategoryFound.setCategoryName(stockCategory.getCategoryName() != null ? stockCategory.getCategoryName()
                    : stockCategoryFound.getCategoryName());

            return stockCategoryRepository.save(stockCategoryFound);
        }).orElse(null);
    }

    @Override
    public StockCategory getSingleEntity(Long entityId) {

        return getEntityList()
                .stream()
                .filter(stockCategory -> stockCategory.getStockCategoryId().equals(entityId))
                .collect(toSingleton());
    }

    @Override
    public List<StockCategory> getEntityList() {

        Set<StockCategory> stockCategoryList = new HashSet<>(Collections.emptySet());

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)
                || AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT)){

            Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());
            List<Seller> sellerList = sellerRepository.findAllByCreatedBy(seller.getCreatedBy());

            stockCategoryList.addAll(sellerList
                    .stream()
                    .map(Seller::getSellerEmail)
                    .map(stockCategoryRepository::findAllByCreatedBy)
                    .flatMap(List::parallelStream)
                    .collect(Collectors.toList()));
            stockCategoryList.addAll(stockCategoryRepository.findAllByCreatedBy(seller.getCreatedBy()));
        }

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

            stockCategoryList.addAll(genericService.sellersByAuthUserId()
                    .stream()
                    .map(Seller::getSellerEmail)
                    .map(stockCategoryRepository::findAllByCreatedBy)
                    .flatMap(List::parallelStream)
                    .collect(Collectors.toList()));

            stockCategoryList.addAll(stockCategoryRepository
                    .findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName()));
        }


        return new ArrayList<>(stockCategoryList);
    }

    @Override
    public StockCategory deleteEntity(Long entityId) {

        if (AuthenticatedUserDetails.getAccount_type() == null ||
                !AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return stockCategoryRepository.findById(entityId).map(stockCategory -> {

            boolean match = getEntityList()
                    .stream()
                    .anyMatch(e -> e.getCreatedBy().equalsIgnoreCase(stockCategory.getCreatedBy()));

            if (match) {
                stockCategoryRepository.delete(stockCategory);
                return stockCategory;
            }
            else throw new InventoryAPIOperationException("Operation not allowed",
                    "You cannot delete a supplier not created by you or any of your sellers", null);
        }).orElse(null);
    }

    @Override
    public StockCategory fetchStockCategoryByName(String stockCategoryName) {

        return getEntityList()
                .stream()
                .filter(stockCategory -> stockCategory.getCategoryName().equalsIgnoreCase(stockCategoryName))
                .collect(toSingleton());
    }

    @Override
    public List<StockCategory> fetchAllStockCategoryByCreatedBy(String createdBy) {

        return getEntityList()
                .stream()
                .filter(stockCategory -> stockCategory.getCreatedBy().equalsIgnoreCase(createdBy))
                .collect(Collectors.toList());
    }
}
