package com.chrisworks.personal.inventorysystem.Backend.Services.StockCategoryServices.Implementation;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockCategory;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.StockCategoryRepository;
import com.chrisworks.personal.inventorysystem.Backend.Services.StockCategoryServices.StockCategoryServices;
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
public class StockCategoryServiceImpl implements StockCategoryServices {

    private final StockCategoryRepository stockCategoryRepository;

    @Autowired
    public StockCategoryServiceImpl(StockCategoryRepository stockCategoryRepository) {
        this.stockCategoryRepository = stockCategoryRepository;
    }

    @Override
    public StockCategory createEntity(StockCategory stockCategory) {

        //for testing purpose
        new AuthenticatedUserDetails(Long.parseLong("1000"), "ETEKA CHRISTOPHER (ADMIN)", ACCOUNT_TYPE.SELLER);
        //

        stockCategory.setCreatedBy(AuthenticatedUserDetails.getUserFullName());

        return stockCategoryRepository.save(stockCategory);
    }

    @Override
    public StockCategory updateEntity(Long entityId, StockCategory stockCategory) {

        AtomicReference<StockCategory> updatedStockCategory = new AtomicReference<>(null);

        stockCategoryRepository.findById(entityId).ifPresent(stockCategoryFound -> {

            stockCategoryFound.setUpdateDate(new Date());
            stockCategoryFound.setCategoryName(stockCategory.getCategoryName());

            updatedStockCategory.set(stockCategoryRepository.save(stockCategoryFound));
        });

        return updatedStockCategory.get();
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

        AtomicReference<StockCategory> deletedStockCategory = new AtomicReference<>(null);

        stockCategoryRepository.findById(entityId).ifPresent(stockCategoryFound -> {

            deletedStockCategory.set(stockCategoryFound);
            stockCategoryRepository.delete(stockCategoryFound);
        });

        return deletedStockCategory.get();
    }

    @Override
    public StockCategory findByStockCategoryName(String stockCategoryName) {

        return stockCategoryRepository.findDistinctFirstByCategoryName(stockCategoryName);
    }
}
