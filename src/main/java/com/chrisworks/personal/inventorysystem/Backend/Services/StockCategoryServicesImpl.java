package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockCategory;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.StockCategoryRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Chris_Eteka
 * @since 12/5/2019
 * @email chriseteka@gmail.com
 */
@Service
public class StockCategoryServicesImpl implements StockCategoryServices {

    private final StockCategoryRepository stockCategoryRepository;

    private final GenericService genericService;

    @Autowired
    public StockCategoryServicesImpl(StockCategoryRepository stockCategoryRepository, GenericService genericService) {
        this.stockCategoryRepository = stockCategoryRepository;
        this.genericService = genericService;
    }

    @Override
    public StockCategory createEntity(StockCategory stockCategory) {

        return genericService.addStockCategory(stockCategory);
    }

    @Override
    public StockCategory updateEntity(Long entityId, StockCategory stockCategory) {

        return stockCategoryRepository.findById(entityId).map(stockCategoryFound -> {

            if (!stockCategoryFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Operation not allowed",
                        "You cannot update a stock category not created by you", null);

            stockCategoryFound.setUpdateDate(new Date());
            stockCategoryFound.setCategoryName(stockCategory.getCategoryName() != null ? stockCategory.getCategoryName()
                    : stockCategoryFound.getCategoryName());

            return stockCategoryRepository.save(stockCategoryFound);
        }).orElse(null);
    }

    @Override
    public StockCategory getSingleEntity(Long entityId) {

        return stockCategoryRepository.findById(entityId).orElse(null);
    }

    @Override
    public List<StockCategory> getEntityList() {

        return stockCategoryRepository.findAll();
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
    public StockCategory findByStockCategoryName(String stockCategoryName) {

        return stockCategoryRepository.findDistinctFirstByCategoryName(stockCategoryName);
    }
}
